# SendPulse Module

## Overview

The SendPulse module provides a NestJS service for interacting with the SendPulse SMTP API. This module allows you to send emails, manage templates, track email delivery, and handle unsubscribes.

## Installation

The module is already included in the notification-service. To use it:

1. Set the `SENDPULSE_ACCESS_TOKEN` environment variable in your `.env` file:
   ```env
   SENDPULSE_ACCESS_TOKEN=your_access_token_here
   ```

2. Import the module in your application module (already done in `app.module.ts`):
   ```typescript
   import { SendPulseModule } from './sendpulse/sendpulse.module';
   
   @Module({
     imports: [SendPulseModule],
     // ...
   })
   export class AppModule {}
   ```

## Usage

### Inject the Service

```typescript
import { Injectable } from '@nestjs/common';
import { SendPulseService } from '../sendpulse/sendpulse.service';

@Injectable()
export class MyService {
  constructor(private readonly sendPulseService: SendPulseService) {}
}
```

### Send an Email with HTML

```typescript
const result = await this.sendPulseService.sendHtmlEmail(
  '<h1>Hello World</h1>',
  'Hello World',
  [{ email: 'recipient@example.com', name: 'John Doe' }],
  { email: 'sender@example.com', name: 'Sender Name' },
  'Test Email',
  {
    cc: [{ email: 'cc@example.com' }],
    bcc: [{ email: 'bcc@example.com' }],
    attachments: { 'file.txt': 'File content' },
    autoPlainText: true,
  },
);
```

### Send an Email Using a Template

```typescript
const result = await this.sendPulseService.sendTemplateEmail(
  123456, // Template ID from SendPulse
  { name: 'John', code: '123456' }, // Template variables
  [{ email: 'recipient@example.com', name: 'John Doe' }],
  { email: 'sender@example.com', name: 'Sender Name' },
  'Test Email',
);
```

### Get Email Information

```typescript
// Get all sent emails
const emails = await this.sendPulseService.getEmails({
  limit: 10,
  offset: 0,
  from: '2024-01-01',
  to: '2024-12-31',
});

// Get specific email
const email = await this.sendPulseService.getEmailById('pzkic9-0afezp-fc');

// Get total emails sent
const total = await this.sendPulseService.getTotalEmails();
```

### Manage Bounces

```typescript
// Get bounces for last 24 hours
const bounces = await this.sendPulseService.getBounces();

// Get bounces for a specific date
const bounces = await this.sendPulseService.getBounces('2024-01-15', 10, 0);

// Get total bounces
const totalBounces = await this.sendPulseService.getTotalBounces();
```

### Manage Unsubscribes

```typescript
// Unsubscribe recipients
await this.sendPulseService.unsubscribe([
  { email: 'user1@example.com', comment: 'User requested' },
  { email: 'user2@example.com', comment: 'Invalid email' },
]);

// Remove from unsubscribed list
await this.sendPulseService.removeFromUnsubscribed(['user1@example.com']);

// Check if email is unsubscribed
const status = await this.sendPulseService.isUnsubscribed('user@example.com');

// Get list of unsubscribed users
const unsubscribed = await this.sendPulseService.getUnsubscribed('2024-01-15', 10, 0);

// Resubscribe a user
await this.sendPulseService.resubscribe(
  'user@example.com',
  'sender@example.com',
  'en',
);
```

### Manage Senders and Domains

```typescript
// Get sender IPs
const ips = await this.sendPulseService.getSenderIps();

// Get sender emails
const senders = await this.sendPulseService.getSenders();

// Add a sender
await this.sendPulseService.addSender('new@example.com', 'New Sender');

// Get sender domains
const domains = await this.sendPulseService.getSenderDomains();

// Add a domain
await this.sendPulseService.addDomain('example.com');
```

## API Reference

### SendPulseService

#### Email Methods

- `sendEmail(emailData: SendPulseEmailRequest)` - Send an email
- `sendTemplateEmail(templateId, variables, to, from, subject)` - Send using template
- `sendHtmlEmail(html, text, to, from, subject, options?)` - Send with HTML

#### Email Information Methods

- `getEmails(filters?)` - Get list of sent emails
- `getEmailById(id)` - Get specific email by ID
- `getEmailsByIds(emailIds)` - Get multiple emails by IDs
- `getTotalEmails()` - Get total count of sent emails

#### Bounce Management

- `getBounces(date?, limit?, offset?)` - Get bounces for 24h period
- `getTotalBounces()` - Get total bounces count

#### Unsubscribe Management

- `unsubscribe(emails)` - Unsubscribe recipients
- `removeFromUnsubscribed(emails)` - Remove from unsubscribe list
- `getUnsubscribed(date?, limit?, offset?)` - Get unsubscribed users
- `isUnsubscribed(email)` - Check if email is unsubscribed
- `resubscribe(email, sender, lang?)` - Resubscribe a user

#### Sender/Domain Management

- `getSenderIps()` - Get sender IP addresses
- `getSenders()` - Get sender email addresses
- `addSender(email, name)` - Add a sender
- `getSenderDomains()` - Get allowed domains
- `addDomain(domain)` - Add a domain

## Configuration

### Environment Variables

| Variable | Description | Required |
|----------|-------------|------------|
| `SENDPULSE_ACCESS_TOKEN` | SendPulse API access token | Yes |

### Getting an Access Token

1. Go to [SendPulse](https://sendpulse.com)
2. Sign up or log in
3. Navigate to API settings
4. Generate an access token
5. Add it to your `.env` file

## Error Handling

The service throws `InternalServerErrorException` for API errors. All errors are logged using NestJS Logger.

## API Documentation

For complete API documentation, see:
- [SendPulse API Documentation](https://sendpulse.com/api)
- [SENDPULSE_SPEC.md](../SENDPULSE_SPEC.md) - Local specification file

## Notes

- The service uses the native `fetch` API (Node.js 18+)
- All HTML content is automatically base64 encoded
- The service is marked as `@Global()` so it can be injected anywhere
- Rate limiting is handled by SendPulse (5 resubscription emails per 24 hours)
