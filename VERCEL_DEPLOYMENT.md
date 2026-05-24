# Vercel Deployment Guide for ResolvIt

## Prerequisites

- Vercel account (sign up at https://vercel.com)
- GitHub repository with the project code
- Node.js and pnpm installed locally (for testing)

## Step 1: Prepare Your Project

1. **Ensure all debug logs are removed**
   - Already completed in this version

2. **Verify environment variables**
   ```bash
   cp .env.example .env.local
   ```

3. **Test locally before deployment**
   ```bash
   pnpm install
   pnpm dev
   # Visit http://localhost:3000
   ```

## Step 2: Deploy to Vercel

### Option A: Using Vercel CLI (Recommended)

```bash
# Install Vercel CLI globally
npm i -g vercel

# Deploy from project root
vercel

# Follow the prompts:
# - Link to existing project or create new
# - Select the framework (Next.js)
# - Accept build settings
```

### Option B: Using GitHub Integration

1. Push your code to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial ResolvIt deployment"
   git remote add origin https://github.com/YOUR_USERNAME/resolvit.git
   git branch -M main
   git push -u origin main
   ```

2. Go to https://vercel.com/new
3. Import your GitHub repository
4. Select the project root
5. Click Deploy

## Step 3: Configure Environment Variables

1. Go to your Vercel project settings
2. Navigate to **Settings → Environment Variables**
3. Add the following variables:

```
NODE_ENV=production
NEXT_PUBLIC_APP_NAME=ResolvIt
NEXT_PUBLIC_APP_URL=https://your-deployment-url.vercel.app
SESSION_SECRET=generate-a-random-string-here
```

## Step 4: Verify Deployment

1. After deployment completes, you'll get a deployment URL
2. Click "Visit" or use the provided URL
3. Test the login with demo credentials:
   - Email: `student@resolvit.com`
   - Password: `password123`

## Production Considerations

### Security
- The `vercel.json` includes security headers (X-Frame-Options, X-Content-Type-Options, etc.)
- Cookies are configured for secure transmission in production
- CORS is properly configured

### Performance
- Next.js automatically optimizes images and code splitting
- API routes are serverless functions
- Vercel CDN caches static assets globally

### Scalability
- In-memory database works for demo; upgrade to Supabase or PostgreSQL for production
- Implement session storage (Redis or Vercel KV) for multiple instances
- Add analytics and monitoring (Sentry, Vercel Analytics)

### Environment-Specific Settings
- Development: `NODE_ENV=development`
- Production: `NODE_ENV=production`
- Staging: Create separate Vercel project

## Troubleshooting

### Build Failures
- Check build logs in Vercel dashboard
- Ensure all dependencies are installed: `pnpm install`
- Verify Node.js version compatibility (18+ recommended)

### Runtime Errors
- Check function logs in Vercel dashboard
- Review application logs with `vercel logs`
- Test API endpoints with curl or Postman

### Session/Authentication Issues
- Clear browser cookies and cache
- Verify SESSION_SECRET is set
- Check cookie settings in `lib/auth.ts`

### Database Issues (if added)
- Ensure database credentials are in environment variables
- Test database connection string locally first
- Use Vercel's integration for managed databases

## Next Steps for Production

1. **Replace in-memory database**
   - Use Supabase, Neon, or PostgreSQL
   - Implement proper user authentication
   - Add password hashing with bcrypt

2. **Add monitoring**
   - Sentry for error tracking
   - Vercel Analytics for performance
   - Custom logging

3. **Implement features**
   - Email notifications for complaint updates
   - Export complaints to PDF
   - Real-time updates with WebSockets

4. **Set up CI/CD**
   - Automatic deployments on git push
   - Staging environment for testing
   - Automatic rollbacks on failure

## Useful Commands

```bash
# View deployment logs
vercel logs

# List all deployments
vercel list

# Redeploy a previous deployment
vercel deploy --prod

# Check environment variables
vercel env ls

# Rollback to previous version
vercel rollback
```

## Support

- Vercel Documentation: https://vercel.com/docs
- Next.js Documentation: https://nextjs.org/docs
- GitHub Issues: Create issues for bugs or feature requests
