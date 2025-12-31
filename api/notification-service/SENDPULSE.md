# SendPulse Integration Guide

## Overview

This document explains how to integrate and use the SendPulse module in the notification-service.

## Setup

### 1. Get SendPulse Access Token

1. Sign up or log in to [SendPulse](https://sendpulse.com)
2. Navigate to Account Settings → API
3. Generate a new access token
4. Copy the token

### 2. Configure Environment Variables

Add the following variables to your `.env` file:

```env
# SendPulse Configuration
SENDPULSE_ACCESS_TOKEN=your_sendpulse_access_token_here
SENDPULSE_SENDER_EMAIL=noreply@compta.tn
SENDPULSE_SENDER_NAME=COMPTA
```

### 3. Choose Email Provider

The notification-service now supports two email providers:

#### Option A: Use SendPulse Processors (Recommended)

The SendPulse processors use SendPulse API directly:

- `SendPulseEmailVerificationProcessor` - For email verification
- `SendPulsePasswordResetProcessor` - For password reset

To use SendPulse processors, simply ensure `SENDPULSE_ACCESS_TOKEN` is configured.

#### Option B: Use Legacy SMTP Processors

The legacy processors use `@nestjs-modules/mailer` with SMTP:

- `EmailVerificationProcessor` - For email verification
- `PasswordResetProcessor` - For password reset

To use SMTP processors, configure the MailerModule in [`app.module.ts`](src/app.module.ts):

```typescript
MailerModule.forRoot({
  transport: {
    host: process.env.SMTP_HOST,
    port: parseInt(process.env.SMTP_PORT) || 587,
    auth: {
      user: process.env.SMTP_USER,
      pass: process.env.SMTP_PASSWORD,
    },
  },
  defaults: {
    from: `"${process.env.SMTP_FROM_NAME}" <${process.env.SMTP_FROM_EMAIL}>`,
  },
}),
```

## Architecture

### Module Structure

```
notification-service/
├── src/
│   ├── sendpulse/
│   │   ├── sendpulse.module.ts      # SendPulse module definition
│   │   ├── sendpulse.service.ts     # SendPulse API service
│   │   └── README.md               # SendPulse module documentation
│   ├── processors/
│   ├── email-verification.processor.ts           # Legacy SMTP processor
│   ├── password-reset.processor.ts               # Legacy SMTP processor
│   ├── sendpulse-email-verification.processor.ts    # SendPulse processor
│   └── sendpulse-password-reset.processor.ts      # SendPulse processor
│   └── app.module.ts            # Main module (imports SendPulseModule)
```

### Queue Configuration

Both SMTP and SendPulse processors use the same BullMQ queues:

- `EMAIL_VERIFICATION` - Email verification queue
- `PASSWORD_RESET` - Password reset queue

Only one processor type should be active per queue to avoid duplicate processing.

## Switching Between Providers

### To Switch to SendPulse

1. Configure `SENDPULSE_ACCESS_TOKEN` in `.env`
2. Remove or comment out legacy processors in `app.module.ts`:
   ```typescript
   // Comment out these:
   // EmailVerificationProcessor,
   // PasswordResetProcessor,
   
   // Keep these:
   SendPulseEmailVerificationProcessor,
   SendPulsePasswordResetProcessor,
   ```

### To Switch to SMTP

1. Configure SMTP settings in `.env`:
   ```env
   SMTP_HOST=smtp.example.com
   SMTP_PORT=587
   SMTP_USER=user@example.com
   SMTP_PASSWORD=your_password
   SMTP_FROM_EMAIL=noreply@compta.tn
   SMTP_FROM_NAME=COMPTA
   ```
2. Configure MailerModule in `app.module.ts` (see above)
3. Remove or comment out SendPulse processors:
   ```typescript
   // Comment out these:
   // SendPulseEmailVerificationProcessor,
   // SendPulsePasswordResetProcessor,
   
   // Keep these:
   EmailVerificationProcessor,
   PasswordResetProcessor,
   ```

## SendPulse API Coverage

The SendPulse service implements the following API endpoints:

### Email Sending
- ✅ Send email with HTML
- ✅ Send email with template
- ✅ Send email with attachments
- ✅ CC and BCC support
- ✅ Auto plain text generation

### Email Information
- ✅ Get list of sent emails
- ✅ Get specific email by ID
- ✅ Get multiple emails by IDs
- ✅ Get total emails sent

### Bounce Management
- ✅ Get bounces for last 24 hours
- ✅ Get bounces for specific date
- ✅ Get total bounces count

### Unsubscribe Management
- ✅ Unsubscribe recipients
- ✅ Remove from unsubscribed list
- ✅ Get unsubscribed users list
- ✅ Check if email is unsubscribed
- ✅ Resubscribe a user

### Sender/Domain Management
- ✅ Get sender IP addresses
- ✅ Get sender email addresses
- ✅ Add sender email
- ✅ Get allowed domains
- ✅ Add domain

## Benefits of SendPulse

1. **API-based**: No need to manage SMTP servers
2. **Analytics**: Built-in email tracking and analytics
3. **Templates**: Use SendPulse templates or send custom HTML
4. **Scalability**: SendPulse handles delivery infrastructure
5. **Reliability**: Built-in retry and bounce handling

## Monitoring

### Bull Board UI

Monitor queues at: `http://localhost:3000/queues`

### SendPulse Dashboard

Monitor email delivery at: https://sendpulse.com

## Troubleshooting

### Issue: Emails not sending

**Check:**
- `SENDPULSE_ACCESS_TOKEN` is set correctly
- Token is valid (not expired)
- Network connectivity to SendPulse API

### Issue: Processor not picking up jobs

**Check:**
- Only one processor type is active per queue
- Bull Board shows jobs in queue
- Processor logs show initialization

### Issue: Templates not loading

**Check:**
- Template files exist in `src/templates/`
- File permissions are correct
- Template path in processor is correct

## API Documentation

- [SendPulse API Docs](https://sendpulse.com/api)
- [SENDPULSE_SPEC.md](./SENDPULSE_SPEC.md) - Local API specification
- [sendpulse/README.md](./src/sendpulse/README.md) - Module documentation
