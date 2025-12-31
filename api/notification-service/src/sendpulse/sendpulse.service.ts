import { Injectable, Logger, InternalServerErrorException } from '@nestjs/common';

/**
 * DTOs for SendPulse API requests and responses
 */

export interface SendPulseEmailRecipient {
  email: string;
  name?: string;
}

export interface SendPulseFrom {
  email: string;
  name: string;
}

export interface SendPulseTemplate {
  id: string | number;
  variables: Record<string, string>;
}

export interface SendPulseEmailRequest {
  html?: string;
  text?: string;
  template?: SendPulseTemplate;
  auto_plain_text?: boolean;
  subject: string;
  from: SendPulseFrom;
  to: SendPulseEmailRecipient[];
  cc?: SendPulseEmailRecipient[];
  bcc?: SendPulseEmailRecipient[];
  attachments?: Record<string, string>;
  attachments_binary?: Record<string, string>;
}

export interface SendPulseSendEmailRequest {
  email: SendPulseEmailRequest;
}

export interface SendPulseSendEmailResponse {
  result: boolean;
  id: string;
}

export interface SendPulseEmailListFilters {
  limit?: number;
  offset?: number;
  from?: string;
  to?: string;
  sender?: string;
  recipient?: string;
  country?: string;
}

export interface SendPulseEmailInfo {
  id: string;
  sender: string;
  total_size: string | number;
  sender_ip: string;
  smtp_answer_code: number | string;
  smtp_answer_subcode: string;
  smtp_answer_data: string;
  used_ip: string;
  recipient: string | null;
  subject: string;
  send_date: string;
  tracking: {
    click: number;
    open: number;
    link?: Array<{
      url: string;
      browser: string;
      os: string;
      screen_resolution: string;
      ip: string;
      country: string;
      action_date: string;
    }>;
    client_info?: Array<{
      browser: string;
      os: string;
      ip: string;
      country: string;
      action_date: string;
    }>;
  };
}

export interface SendPulseTotalEmailsResponse {
  total: number;
}

export interface SendPulseBounce {
  email_to: string;
  sender: string;
  send_date: string;
  subject: string;
  smtp_answer_code: number;
  smtp_answer_subcode: string;
  smtp_answer_data: string;
}

export interface SendPulseBouncesResponse {
  total: number;
  emails: SendPulseBounce[];
  request_limit: number;
  found: number;
}

export interface SendPulseUnsubscribeRequest {
  email: string;
  comment?: string;
}

export interface SendPulseUnsubscribeResponse {
  result: boolean;
}

export interface SendPulseUnsubscribeInfo {
  email: string;
  unsubscribe_by_link: number;
  unsubscribe_by_user: number;
  spam_complaint: number;
  date: string;
}

export interface SendPulseResubscribeRequest {
  email: string;
  sender: string;
  lang?: 'ru' | 'en' | 'ua' | 'tr' | 'es' | 'pt';
}

export interface SendPulseResubscribeResponse {
  result: boolean;
  id: string;
}

export interface SendPulseSenderDomain {
  id: number;
  user_id: number;
  service_type: number;
  service_value: string;
  status: number;
  expire_date: string | null;
  auto_free_prolong: number;
  currency: string;
  is_default: boolean;
  ssl_type: number;
  ssl_expired: string | null;
  ssl_generated: number | null;
  checks: {
    check_dkim: boolean;
    check_spf: boolean;
    check_dmarc: boolean;
    all_checks: boolean;
    spf_txt_needed: string;
  };
}

export interface SendPulseSenderDomainsResponse {
  data: {
    result: boolean;
    data: SendPulseSenderDomain[];
  };
}

export interface SendPulseAddSenderRequest {
  email: string;
  name: string;
}

export interface SendPulseAddSenderResponse {
  result: boolean;
}

export interface SendPulseAddDomainResponse {
  data: {
    result: boolean;
    error: string | null;
  };
}

/**
 * SendPulse Service
 *
 * Service for interacting with SendPulse SMTP API.
 * Documentation: https://sendpulse.com/api
 *
 * @see SENDPULSE_SPEC.md for API specification
 */
@Injectable()
export class SendPulseService {
  private readonly logger = new Logger(SendPulseService.name);
  private readonly baseUrl = 'https://api.sendpulse.com';
  private readonly accessToken: string;

  constructor() {
    this.accessToken = process.env.SENDPULSE_ACCESS_TOKEN || '';

    if (!this.accessToken) {
      this.logger.warn('SENDPULSE_ACCESS_TOKEN is not configured');
    }
  }

  /**
   * Helper method to make authenticated requests to SendPulse API
   */
  private async request<T>(
    method: 'GET' | 'POST' | 'DELETE',
    endpoint: string,
    data?: any,
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    const headers: HeadersInit = {
      'Authorization': `Bearer ${this.accessToken}`,
      'Content-Type': 'application/json',
    };

    try {
      this.logger.debug(`SendPulse ${method} ${endpoint}`);

      let response: Response;

      if (method === 'GET') {
        response = await fetch(url, {
          method: 'GET',
          headers,
        });
      } else if (method === 'POST') {
        response = await fetch(url, {
          method: 'POST',
          headers,
          body: JSON.stringify(data),
        });
      } else if (method === 'DELETE') {
        response = await fetch(url, {
          method: 'DELETE',
          headers,
          body: JSON.stringify(data),
        });
      } else {
        throw new Error(`Unsupported HTTP method: ${method}`);
      }

      if (!response.ok) {
        const errorText = await response.text();
        this.logger.error(
          `SendPulse API error: ${response.status} ${response.statusText}`,
          errorText,
        );
        throw new InternalServerErrorException(
          `SendPulse API error: ${response.status} ${response.statusText}`,
        );
      }

      return response.json() as Promise<T>;
    } catch (error) {
      if (error instanceof InternalServerErrorException) {
        throw error;
      }
      this.logger.error(`SendPulse request error: ${error.message}`, error.stack);
      throw new InternalServerErrorException(
        `SendPulse request error: ${error.message}`,
      );
    }
  }

  /**
   * Send an email via SendPulse SMTP
   *
   * POST /smtp/emails
   *
   * @param emailData Email data with html/text or template
   * @returns SendPulse response with email ID
   */
  async sendEmail(emailData: SendPulseEmailRequest): Promise<SendPulseSendEmailResponse> {
    const request: SendPulseSendEmailRequest = { email: emailData };

    this.logger.log(`Sending email to ${emailData.to.map(t => t.email).join(', ')}`);

    return this.request<SendPulseSendEmailResponse>('POST', '/smtp/emails', request);
  }

  /**
   * Send an email using a SendPulse template
   *
   * @param templateId Template ID from SendPulse
   * @param variables Template variables
   * @param to Recipients
   * @param from Sender info
   * @param subject Email subject
   * @returns SendPulse response with email ID
   */
  async sendTemplateEmail(
    templateId: string | number,
    variables: Record<string, string>,
    to: SendPulseEmailRecipient[],
    from: SendPulseFrom,
    subject: string,
  ): Promise<SendPulseSendEmailResponse> {
    const emailData: SendPulseEmailRequest = {
      subject,
      from,
      to,
      template: {
        id: templateId,
        variables,
      },
    };

    return this.sendEmail(emailData);
  }

  /**
   * Send an email with custom HTML
   *
   * @param html HTML content (will be base64 encoded)
   * @param text Plain text version
   * @param to Recipients
   * @param from Sender info
   * @param subject Email subject
   * @returns SendPulse response with email ID
   */
  async sendHtmlEmail(
    html: string,
    text: string,
    to: SendPulseEmailRecipient[],
    from: SendPulseFrom,
    subject: string,
    options?: {
      cc?: SendPulseEmailRecipient[];
      bcc?: SendPulseEmailRecipient[];
      attachments?: Record<string, string>;
      autoPlainText?: boolean;
    },
  ): Promise<SendPulseSendEmailResponse> {
    const emailData: SendPulseEmailRequest = {
      html: Buffer.from(html).toString('base64'),
      text,
      subject,
      from,
      to,
      auto_plain_text: options?.autoPlainText || false,
      cc: options?.cc,
      bcc: options?.bcc,
      attachments: options?.attachments,
    };

    return this.sendEmail(emailData);
  }

  /**
   * Get a list of sent emails
   *
   * GET /smtp/emails
   *
   * @param filters Optional filters
   * @returns List of sent emails
   */
  async getEmails(filters?: SendPulseEmailListFilters): Promise<SendPulseEmailInfo[]> {
    const params = new URLSearchParams();

    if (filters?.limit) params.append('limit', filters.limit.toString());
    if (filters?.offset) params.append('offset', filters.offset.toString());
    if (filters?.from) params.append('from', filters.from);
    if (filters?.to) params.append('to', filters.to);
    if (filters?.sender) params.append('sender', filters.sender);
    if (filters?.recipient) params.append('recipient', filters.recipient);
    if (filters?.country) params.append('country', filters.country);

    const queryString = params.toString();
    const endpoint = queryString ? `/smtp/emails?${queryString}` : '/smtp/emails';

    return this.request<SendPulseEmailInfo[]>('GET', endpoint);
  }

  /**
   * Get total amount of sent emails
   *
   * GET /smtp/emails/total
   *
   * @returns Total count of sent emails
   */
  async getTotalEmails(): Promise<SendPulseTotalEmailsResponse> {
    return this.request<SendPulseTotalEmailsResponse>('GET', '/smtp/emails/total');
  }

  /**
   * Get information about a specific email
   *
   * GET /smtp/emails/{id}
   *
   * @param id Email ID
   * @returns Email information
   */
  async getEmailById(id: string): Promise<SendPulseEmailInfo> {
    return this.request<SendPulseEmailInfo>('GET', `/smtp/emails/${id}`);
  }

  /**
   * Get information for a list of emails
   *
   * POST /smtp/emails/info
   *
   * @param emailIds Array of email IDs (max 500)
   * @returns Array of email information
   */
  async getEmailsByIds(emailIds: string[]): Promise<SendPulseEmailInfo[]> {
    return this.request<SendPulseEmailInfo[]>('POST', '/smtp/emails/info', emailIds);
  }

  /**
   * Get information about bounces for a 24-hour period
   *
   * GET /smtp/bounces/day
   *
   * @param date Day in YYYY-MM-DD format (optional, defaults to last 24h)
   * @param limit Number of records (optional)
   * @param offset Offset (optional)
   * @returns Bounces information
   */
  async getBounces(
    date?: string,
    limit?: number,
    offset?: number,
  ): Promise<SendPulseBouncesResponse> {
    const params = new URLSearchParams();

    if (date) params.append('date', date);
    if (limit) params.append('limit', limit.toString());
    if (offset) params.append('offset', offset.toString());

    const queryString = params.toString();
    const endpoint = queryString ? `/smtp/bounces/day?${queryString}` : '/smtp/bounces/day';

    return this.request<SendPulseBouncesResponse>('GET', endpoint);
  }

  /**
   * Get total number of bounces
   *
   * GET /smtp/bounces/day/total
   *
   * @returns Total bounces count
   */
  async getTotalBounces(): Promise<{ total: number }> {
    return this.request<{ total: number }>('GET', '/smtp/bounces/day/total');
  }

  /**
   * Unsubscribe recipients
   *
   * POST /smtp/unsubscribe
   *
   * @param emails Array of emails to unsubscribe with optional comments
   * @returns Result
   */
  async unsubscribe(emails: SendPulseUnsubscribeRequest[]): Promise<SendPulseUnsubscribeResponse> {
    this.logger.log(`Unsubscribing ${emails.length} emails`);
    return this.request<SendPulseUnsubscribeResponse>('POST', '/smtp/unsubscribe', emails);
  }

  /**
   * Remove emails from unsubscribed list
   *
   * DELETE /smtp/unsubscribe
   *
   * @param emails Array of emails to remove
   * @returns Result
   */
  async removeFromUnsubscribed(emails: string[]): Promise<SendPulseUnsubscribeResponse> {
    this.logger.log(`Removing ${emails.length} emails from unsubscribed list`);
    return this.request<SendPulseUnsubscribeResponse>('DELETE', '/smtp/unsubscribe', emails);
  }

  /**
   * Get a list of unsubscribed users
   *
   * GET /smtp/unsubscribe
   *
   * @param date Specific day (YYYY-MM-DD) or undefined for all
   * @param limit Number of records
   * @param offset Offset
   * @returns List of unsubscribed users
   */
  async getUnsubscribed(
    date?: string,
    limit?: number,
    offset?: number,
  ): Promise<SendPulseUnsubscribeInfo[]> {
    const params = new URLSearchParams();

    if (date) params.append('date', date);
    if (limit) params.append('limit', limit.toString());
    if (offset) params.append('offset', offset.toString());

    const queryString = params.toString();
    const endpoint = queryString ? `/smtp/unsubscribe?${queryString}` : '/smtp/unsubscribe';

    return this.request<SendPulseUnsubscribeInfo[]>('GET', endpoint);
  }

  /**
   * Check if a contact is in unsubscribed list
   *
   * GET /smtp/unsubscribe/search?email={email}
   *
   * @param email Email address to check
   * @returns Result (true if unsubscribed)
   */
  async isUnsubscribed(email: string): Promise<{ result: boolean }> {
    return this.request<{ result: boolean }>('GET', `/smtp/unsubscribe/search?email=${encodeURIComponent(email)}`);
  }

  /**
   * Resubscribe a recipient
   *
   * POST /smtp/resubscribe
   *
   * Note: Only 5 resubscription emails per 24 hours per account
   *
   * @param email Recipient's email
   * @param sender Sender's email
   * @param lang Confirmation email language (default: 'en')
   * @returns Result with sent email ID
   */
  async resubscribe(
    email: string,
    sender: string,
    lang: 'ru' | 'en' | 'ua' | 'tr' | 'es' | 'pt' = 'en',
  ): Promise<SendPulseResubscribeResponse> {
    const request: SendPulseResubscribeRequest = { email, sender, lang };

    this.logger.log(`Resubscribing ${email} with sender ${sender}`);
    return this.request<SendPulseResubscribeResponse>('POST', '/smtp/resubscribe', request);
  }

  /**
   * Get a list of sender's IP addresses
   *
   * GET /smtp/ips
   *
   * @returns Array of IP addresses
   */
  async getSenderIps(): Promise<string[]> {
    return this.request<string[]>('GET', '/smtp/ips');
  }

  /**
   * Get a list of sender's email addresses
   *
   * GET /smtp/senders
   *
   * @returns Array of sender email addresses
   */
  async getSenders(): Promise<string[]> {
    return this.request<string[]>('GET', '/smtp/senders');
  }

  /**
   * Get a list of allowed domains
   *
   * GET /v2/email-service/smtp/sender_domains
   *
   * @returns Sender domains information
   */
  async getSenderDomains(): Promise<SendPulseSenderDomainsResponse> {
    return this.request<SendPulseSenderDomainsResponse>('GET', '/v2/email-service/smtp/sender_domains');
  }

  /**
   * Add a sender email
   *
   * POST /senders
   *
   * @param email Sender's email
   * @param name Sender's name
   * @returns Result
   */
  async addSender(email: string, name: string): Promise<SendPulseAddSenderResponse> {
    const request: SendPulseAddSenderRequest = { email, name };

    this.logger.log(`Adding sender: ${email} (${name})`);
    return this.request<SendPulseAddSenderResponse>('POST', '/senders', request);
  }

  /**
   * Add a sender domain
   *
   * POST /v2/email-service/smtp/sender_domains/{domain}
   *
   * @param domain Domain to add
   * @returns Result
   */
  async addDomain(domain: string): Promise<SendPulseAddDomainResponse> {
    this.logger.log(`Adding domain: ${domain}`);
    return this.request<SendPulseAddDomainResponse>(
      'POST',
      `/v2/email-service/smtp/sender_domains/${domain}`,
    );
  }
}
