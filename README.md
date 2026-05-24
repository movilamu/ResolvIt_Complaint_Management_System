# ResolvIt - Complaint Management System

A modern web-based complaint management system built with Next.js, React, and Tailwind CSS. Designed for institutions to efficiently manage and resolve student complaints.

## Features

- **Multi-role Support**: Student, Admin, and SuperAdmin roles
- **Complaint Management**: Create, track, and manage complaints with status updates
- **Real-time Dashboard**: View all complaints with filtering and sorting
- **Dark/Light Theme**: Professional dark-themed interface
- **Secure Authentication**: Session-based authentication with secure cookies
- **Responsive Design**: Mobile-friendly interface
- **Demo Data**: Pre-populated sample data for testing

## Tech Stack

- **Frontend**: Next.js 16, React 19, Tailwind CSS v4
- **Backend**: Next.js API Routes (Serverless)
- **Database**: In-memory storage (upgradeable to PostgreSQL/Supabase)
- **Styling**: Tailwind CSS with custom components
- **Deployment**: Vercel

## Quick Start

### Local Development

```bash
# Install dependencies
pnpm install

# Run development server
pnpm dev

# Open browser
open http://localhost:3000
```

### Demo Credentials

| Role       | Email                    | Password    |
|-----------|--------------------------|------------|
| Student   | student@resolvit.com    | password123 |
| Admin     | admin@resolvit.com      | password123 |
| SuperAdmin| superadmin@resolvit.com | password123 |

## Project Structure

```
resolvit/
├── app/
│   ├── api/                 # API routes
│   │   ├── auth/            # Authentication endpoints
│   │   └── complaints/      # Complaints CRUD
│   ├── dashboard/           # Main dashboard page
│   ├── login/               # Login page
│   └── layout.tsx           # Root layout
├── components/              # Reusable components
├── lib/
│   ├── db.ts               # Database functions
│   └── auth.ts             # Authentication utilities
├── public/                  # Static assets
├── styles/                  # Global styles
├── vercel.json             # Vercel configuration
└── next.config.mjs         # Next.js configuration
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user
- `POST /api/auth/logout` - User logout

### Complaints
- `GET /api/complaints` - List user complaints
- `POST /api/complaints` - Create complaint
- `PUT /api/complaints/:id` - Update complaint (Admin)
- `DELETE /api/complaints/:id` - Delete complaint (Admin)

## Deployment

### Deploy to Vercel

```bash
# Option 1: Using Vercel CLI
vercel

# Option 2: Using GitHub Integration
# Connect repository to Vercel dashboard at https://vercel.com/new
```

See [VERCEL_DEPLOYMENT.md](./VERCEL_DEPLOYMENT.md) for detailed deployment instructions.

## Environment Variables

Create a `.env.local` file:

```
NODE_ENV=production
NEXT_PUBLIC_APP_NAME=ResolvIt
NEXT_PUBLIC_APP_URL=http://localhost:3000
SESSION_SECRET=your-secret-key
```

See `.env.example` for all available variables.

## Development

### Build for Production

```bash
pnpm build
pnpm start
```

### Run Tests

```bash
# Unit tests
pnpm test

# End-to-end tests
pnpm test:e2e
```

### Lint and Format

```bash
# Lint code
pnpm lint

# Format code
pnpm format
```

## Future Enhancements

- [ ] Database integration (PostgreSQL/Supabase)
- [ ] Email notifications
- [ ] PDF export for complaints
- [ ] Advanced analytics dashboard
- [ ] Real-time updates with WebSockets
- [ ] File uploads for complaints
- [ ] Complaint templates
- [ ] Assignment workflows
- [ ] SLA tracking
- [ ] User feedback system

## Security

- Secure session management with HTTP-only cookies
- CSRF protection
- XSS prevention with React's built-in escaping
- SQL injection prevention (when using ORM)
- Security headers configured in `vercel.json`

## Performance

- Server-side rendering with Next.js
- Image optimization
- Code splitting and lazy loading
- CDN caching with Vercel
- Optimized API routes

## Contributing

1. Create a feature branch
2. Make your changes
3. Submit a pull request
4. Await review and merge

## License

MIT License - feel free to use this project for personal or commercial purposes.

## Support

- Issues: GitHub Issues
- Documentation: See `/docs` folder
- Deployment: See `VERCEL_DEPLOYMENT.md`

## Author

Created as a modern web-based alternative to the original JavaFX desktop application.

---

**Version**: 1.0.0  
**Last Updated**: 2026  
**Status**: Production Ready
