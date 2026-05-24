# Quick Deployment to Vercel - 5 Minutes

Follow these steps to deploy ResolvIt to Vercel in 5 minutes.

## Step 1: Prepare Code (1 minute)

```bash
# Make sure all changes are committed
git add .
git commit -m "Prepare for Vercel deployment"
git push origin main
```

## Step 2: Connect to Vercel (2 minutes)

### Option A: Using Vercel Dashboard (Easiest)

1. Go to https://vercel.com/new
2. Click "Continue with GitHub"
3. Select your repository
4. Click "Import"
5. Click "Deploy"

### Option B: Using Vercel CLI

```bash
# Install Vercel CLI
npm i -g vercel

# Deploy from project root
vercel

# Follow prompts and confirm deployment
```

## Step 3: Set Environment Variables (1 minute)

In Vercel Dashboard:

1. Go to your project
2. Settings → Environment Variables
3. Add these variables:

```
NODE_ENV = production
NEXT_PUBLIC_APP_NAME = ResolvIt
NEXT_PUBLIC_APP_URL = https://your-project.vercel.app
SESSION_SECRET = (generate a random string)
```

## Step 4: Verify Deployment (1 minute)

1. Wait for build to complete (usually 2-3 minutes)
2. Click "Visit" to see your live site
3. Test with credentials:
   - Email: `student@resolvit.com`
   - Password: `password123`

## Your Site is Live! 🎉

Your ResolvIt application is now running on Vercel!

### Production URL

Your deployment URL will be: `https://resolvit-[random].vercel.app`

Or use a custom domain by going to Settings → Domains in Vercel.

### Next Steps

1. **Monitor Performance**
   - Go to Analytics in Vercel dashboard
   - Check function logs in Logs tab

2. **Set Up Continuous Deployment**
   - Automatic deployments are already enabled for pushes to main
   - Preview deployments work on pull requests

3. **Upgrade for Production**
   - Add a real database (Supabase, PostgreSQL)
   - Add email service for notifications
   - Implement user management
   - Add analytics and monitoring

4. **Custom Domain (Optional)**
   - Add your domain in Settings → Domains
   - Follow DNS configuration instructions

## Troubleshooting

### Build Failed
- Check build logs in Vercel dashboard
- Ensure all dependencies installed locally: `pnpm install`
- Verify Node version 18+

### Login Not Working
- Clear browser cookies
- Check Environment Variables are set
- Verify SESSION_SECRET is configured

### Performance Issues
- Check Analytics in dashboard
- Review function duration times
- Consider database optimization

## Key Features to Show

1. **Login Page**
   - Beautiful dark-themed login
   - Pre-filled demo credentials
   - Responsive design

2. **Dashboard**
   - Sidebar navigation
   - Complaint management interface
   - User profile display

3. **Complaint Management**
   - Create new complaints
   - View complaint list
   - Track complaint status

## Important

**For Production Use:**
- Replace in-memory database with Supabase or PostgreSQL
- Implement proper user authentication
- Add email notifications
- Set up monitoring and alerts
- Configure backups

See `VERCEL_DEPLOYMENT.md` for detailed production setup.

---

**That's it!** Your ResolvIt application is now live on Vercel. 🚀
