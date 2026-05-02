package com.shopping.cart.controller.api;

import com.shopping.cart.service.CheckoutFulfillmentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final CheckoutFulfillmentService checkoutFulfillmentService;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    public StripeWebhookController(CheckoutFulfillmentService checkoutFulfillmentService) {
        this.checkoutFulfillmentService = checkoutFulfillmentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                          @RequestHeader(name = "Stripe-Signature", required = false) String sigHeader) {
        // If no webhook secret is configured, do not process events.
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            return ResponseEntity.status(501).body("Stripe webhook not configured");
        }
        if (sigHeader == null || sigHeader.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature header");
        }

        final Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Object dataObject = event.getDataObjectDeserializer().getObject().orElse(null);
            if (dataObject instanceof Session session) {
                try {
                    checkoutFulfillmentService.fulfillBySessionId(session.getId());
                } catch (Exception e) {
                    // Returning 500 makes Stripe retry; keep it simple for now.
                    return ResponseEntity.status(500).body("Webhook processing failed");
                }
            }
        }

        return ResponseEntity.ok("ok");
    }
}

