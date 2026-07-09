# Production deployment (Railway)

Use this after pushing this repo to GitHub. **Redeploy** when env vars change.

## Repository layout

| Path | Role |
|------|------|
| `LICENSE`, `README.md`, `DEPLOY.md` | Repo docs |
| `railway.toml` | Railway deploy settings |
| `Dockerfile`, `.dockerignore` | API Docker image |

The Flutter client lives in a **separate repo** — set `APP_FRONTEND_BASE_URL` to your Vercel URL from that project.

## Railway (API)

1. Push this repo to GitHub.
2. [Railway](https://railway.app) → **New Project** → **Deploy from GitHub repo** → select this repository.
3. Railway detects the `Dockerfile` (see [`railway.toml`](railway.toml) for health check path).
4. In **Variables**, add:

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

5. **Settings → Networking** → generate a public domain (e.g. `your-service.up.railway.app`).
6. Copy that URL into the Flutter repo (`API_BASE_URL` and `vercel.json` proxy — see Flutter `DEPLOY.md`).

**Stripe webhook**

1. Stripe Dashboard → Developers → Webhooks → Add endpoint  
2. URL: `https://YOUR-SERVICE.up.railway.app/api/stripe/webhook`  
3. Event: `checkout.session.completed`  
4. Copy signing secret → `STRIPE_WEBHOOK_SECRET` on Railway → redeploy  

**Verify API**

```bash
curl https://YOUR-SERVICE.up.railway.app/api/products/index
curl https://YOUR-SERVICE.up.railway.app/actuator/health
```

Expect JSON from `/api/products/index`, not HTML.

## Admin catalog

1. Set `ADMIN_SEED_USERNAME` / `ADMIN_SEED_PASSWORD` on Railway and redeploy **once**, or promote a user to Admin in Neon:

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
| Railway build fails | Ensure repo root contains `Dockerfile` (no monorepo subfolder) |
| Railway deploy: app crashes / health check fails | Set `DATABASE_URL` to Neon `postgresql://…?sslmode=require` |
