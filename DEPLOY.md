# Production deployment (Render)

Use this after pushing this repo to GitHub. **Redeploy** when env vars change.

## Repository layout

| Path | Role |
|------|------|
| `LICENSE`, `README.md`, `DEPLOY.md` | Repo docs |
| `render.yaml` | Render Blueprint (repo root) |
| `Dockerfile`, `.dockerignore` | API Docker image |

The Flutter client lives in a **separate repo** — set `APP_FRONTEND_BASE_URL` to your Vercel URL from that project.

## Render (API)

**Root Directory:** leave blank (repo root). The `Dockerfile` is at the root of this repository.

**Blueprint deploy:** use [`render.yaml`](render.yaml) at the repo root.

Set in **Render → Environment**:

| Variable | Example / notes |
|----------|-----------------|
| `DATABASE_URL` | Neon `postgresql://…?sslmode=require` |
| `JWT_SECRET` | `openssl rand -base64 32` |
| `STRIPE_API_KEY` | `sk_test_…` or live key |
| `STRIPE_WEBHOOK_SECRET` | From Stripe Dashboard → Webhooks → signing secret (`whsec_…`) |
| `APP_FRONTEND_BASE_URL` | `https://your-app.vercel.app` (no trailing slash OK) |
| `CLOUDINARY_CLOUD_NAME` | Your Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | API key |
| `CLOUDINARY_API_SECRET` | API secret |
| `ADMIN_SEED_USERNAME` | Optional — first admin login (e.g. `admin`) |
| `ADMIN_SEED_PASSWORD` | Optional — only used if username does not exist yet |

**Stripe webhook**

1. Stripe Dashboard → Developers → Webhooks → Add endpoint  
2. URL: `https://YOUR-RENDER-SERVICE.onrender.com/api/stripe/webhook`  
3. Event: `checkout.session.completed`  
4. Copy signing secret → `STRIPE_WEBHOOK_SECRET` on Render → redeploy  

**Verify API**

```bash
curl https://YOUR-RENDER-SERVICE.onrender.com/api/products/index
curl https://YOUR-RENDER-SERVICE.onrender.com/actuator/health
```

Expect JSON from `/api/products/index`, not HTML.

## Admin catalog

1. Set `ADMIN_SEED_USERNAME` / `ADMIN_SEED_PASSWORD` on Render and redeploy **once**, or promote a user to Admin in Neon:

   ```sql
   UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'Admin') WHERE username = 'youruser';
   ```

2. Sign in on the app → **Me** → **Manage products**  
3. Add products with images (uploaded to Cloudinary)

On startup, the API also migrates legacy image paths like `seed-headphones.jpg` to HTTPS placeholder URLs.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| CORS error in browser console | `APP_FRONTEND_BASE_URL` must match your Vercel URL |
| Paid but no order | Configure Stripe webhook + `STRIPE_WEBHOOK_SECRET` |
| Broken product images | Re-upload via admin, or restart API (image migration runs on boot) |
| No “Manage products” in Me | User must have Admin role |
| Render build: `Dockerfile: no such file or directory` | Root Directory must be blank (this repo root) |
| Render deploy: app crashes / health check fails | Set `DATABASE_URL` to Neon `postgresql://…?sslmode=require` |
