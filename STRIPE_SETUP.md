# Stripe Configuration

## Local Development

1. Create `src/main/resources/application-secrets.properties` (ignored by git):
```properties
stripe.secret.key=sk_test_your_key_here
stripe.publishable.key=pk_test_your_key_here
```

2. This file is automatically loaded by Spring Boot and will NOT be committed.

## Production Deployment

Set environment variables in your deployment platform:

**Railway:**
```bash
STRIPE_SECRET_KEY=sk_live_your_production_key
STRIPE_PUBLISHABLE_KEY=pk_live_your_production_key
```

**Heroku:**
```bash
heroku config:set STRIPE_SECRET_KEY=sk_live_your_production_key
heroku config:set STRIPE_PUBLISHABLE_KEY=pk_live_your_production_key
```

**Docker:**
```bash
docker run -e STRIPE_SECRET_KEY=sk_live_xxx -e STRIPE_PUBLISHABLE_KEY=pk_live_xxx ...
```

## Security

- ✅ `application-secrets.properties` is in `.gitignore`
- ✅ Never commit API keys to git
- ✅ Use test keys for development (prefix: `sk_test_`, `pk_test_`)
- ✅ Use live keys for production (prefix: `sk_live_`, `pk_live_`)
