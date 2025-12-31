Ceci est la SPEC de SENDPULSE pour envoyer des emails avec sendpulse

SMTP Service API
SMTP will only work if the SMTP account is activated beforehand. Fill out the application form to get started. Learn more: How to get started with SMTP.

Send an email
To send an email, send a POST request to:

https://api.sendpulse.com/smtp/emails
Request parameters:

Parameter	Type	Description	 
email	object	Serialized object with email data	required
Email array parameters:

Parameter	Type	Description	 
html	string	HTML version of an email, encoded in Base64	required*
text	string	Text version of the email	required*
template	array	Template:	required*
id	string/int	Template ID that was uploaded in the service. Use Get a list of templates on an acoount method to get the template ID (use either real_id or id parameter from the response) 	required
variables	object	Includes variables' names and values in such a format:  {"name_of_the_variable": "value_of_the_variable"}	
required

auto_plain_text	boolean	Type parameter, whether or not to automatically generate a text part for messages that are not given text; default is false	required
subject	string	Email subject	required
from	object	An array containing the sender’s name and email address	required
to	array	An array of recipients	required
сс	array	An array of recipients who will receive a copy of the email. Recipients will see who received an email copy	optional
bcc	array	An array of recipients who will receive a blind carbon copy of the email. Recipients will not see who received an email copy	optional
attachments	object	An object where the key is the name of the file, and the value is the contents of the file, for example, {"name_of_the_file": "contents_of_the_file"}	optional
attachments_binary	object	An object where the key is the name of the file, and the value is the contents of the file, encoded in Base64	optional
*Note: You can use parameter html and text to send the template directly in this request.

Or you can use the parameter template and send a static template uploaded to the email service and send only values for these variables (which were added in template).

Example of sending a custom template:

{
  "email": {
    "html": "PHA+RXhhbXBsZSB0ZXh0PC9wPg==",
    "text": "Example text",
    "subject": "Example subject",
    "from": {
      "name": "Example name",
      "email": "sender@example.com"
    },
    "to": [
      {
        "name": "Recipient1 name",
        "email": "recipient1@example.com"
      }
    ]
  }
}

Example of sending a system template with custom variables:

{
  "email": {
    "subject": "Test",
    "template": {
      "id": 123456,
      "variables": {
        "name": "George",
        "code": "123456"
      }
    },
    "from": {
      "name": "Mike",
      "email": "mike.johnson@domain.com"
    },
    "to": [
      {
        "email": "recipient1@example.com",
        "name": "George"
      }
    ]
  }
}

In this example, a template parameter is used instead of html and text parameters.

Example of sending email copies:

{
  "email": {
    "html":"dGVzdCBBUEk=",
    "text": "text",
    "subject": "subject",
    "from": {
      "name": "test",
      "email": "test@test.com"
    },
    "to": [
      {
        "name": "recipient 1",
        "email": "recipient1@test.com"
      }
    ],
    "cc":[
      {
         "name":"recipient 2",
         "email":"recipient2@test.com" 
      }
    ],
    "bcc":[
      {
         "name":"recipient 3",
         "email":"recipient3@test.com" 
      }
    ]
  }
}

Example of sending an email with attachments:

{
  "email": {
    "html": "PHA+RXhhbXBsZSB0ZXh0PC9wPg==",
    "text": "Example text b",
    "subject": "Example subject b",
    "from": {
      "name": "Example name",
      "email": "sender@example.com"
    },
    "to": [
      {
        "name": "Recipient1 name",
        "email": "recipient1@example.com"
      }
    ],
    "attachments":{
        "file1.txt":"file1 text content",
        "file2.txt":"file2 text content"
    }
  }
}

Example of sending an email with attachments_binary:

{
  "email": {
    "html": "PHA+RXhhbXBsZSB0ZXh0PC9wPg==",
    "text": "Example text b",
    "subject": "Example subject b",
    "from": {
      "name": "Example name",
      "email": "sender@example.com"
    },
    "to": [
      {
        "name": "Recipient1 name",
        "email": "recipient1@example.com"
      }
    ],
    "attachments_binary":{
        "file1.txt":"bXkgdGV4dA==",
        "file2.txt":"bXkgdGV4dA=="
    }
  }
}

If the request is successful, you will receive a response:

{
    "result": true,
    "id": "pzkic9-0afezp-fc"
}

Get a list of emails
To get a list of all sent emails, send a GET request to:

https://api.sendpulse.com/smtp/emails
Request parameters:

Parameter	Type	Description	 
limit	int	Number of records	optional
offset	int	Offset (first record to be displayed) 	optional
from	string	Start date	optional
to	string	End date	optional
sender	string	Sender	optional
recipient	string	Recipient	optional
country	string	When off, country will not be specified	optional
If the request is successful, you will receive a response:

{
   "id":"pzkic9-0afezp-fc",
   "sender":"JohnDoe@test.com",
   "total_size":1128,
   "sender_ip":"127.0.0.1",
   "smtp_answer_code":250,
   "smtp_answer_subcode":"0",
   "smtp_answer_data":"Bad recipients",
   "used_ip":"5.104.224.87",
   "recipient":null,
   "subject":"SendPulse :: Email confirmation",
   "send_date":"2013-12-17 10:33:53",
   "tracking":{
      "click":1,
      "open":1,
      "link":[
         {
            "url":"http://some-url.com",
            "browser":"Chrome 29.0.1547.57",
            "os":"Linux",
            "screen_resolution":"1920x1080",
            "ip":"46.149.83.86",
            "country":"USA",
            "action_date":"2013-09-30 11:27:40"
         }
      ],
      "client_info":[
         {
            "browser":"Thunderbird 17.0.8",
            "os":"Linux",
            "ip":"46.149.83.86",
            "country":"USA",
            "action_date":"2013-09-30 11:27:49"
         }
      ]
   }
}

Get total amount of sent emails
To get a total amount of send emails, send a GET request to:

https://api.sendpulse.com/smtp/emails/total
If the request is successful, you will receive a response:

{
   "total": 25408
}

Get information about a specific email
To get information about a specific email, send a GET request to:

https://api.sendpulse.com/smtp/emails/{id}
Request parameter:

Parameter	Type	Description	 
id	string	Email address ID	required
If the request is successful, you will receive a response:

{
   "id":"pzkic9-0afezp-fc",
   "sender":"JohnDoe@test.com",
   "total_size":"1128",
   "sender_ip":"127.0.0.1",
   "smtp_answer_code":"250",
   "smtp_answer_subcode":"0",
   "smtp_answer_data":"Bad recipients",
   "used_ip":"5.104.224.87",
   "recipient":null,
   "subject":"SendPulse :: Email confirmation",
   "send_date":"2013-12-17 10:33:53",
   "tracking":{
      "click":1,
      "open":1,
      "link":[
         {
            "url":"http://some-url.com",
            "browser":"Chrome 29.0.1547.57",
            "os":"Linux",
            "screen_resolution":"1920x1080",
            "ip":"46.149.83.86",
            "country":"Ukraine",
            "action_date":"2013-09-30 11:27:40"
         }
      ],
      "client_info":[
         {
            "browser":"Thunderbird 17.0.8",
            "os":"Linux",
            "ip":"46.149.83.86",
            "country":"USA",
            "action_date":"2013-09-30 11:27:49"
         }
      ]
   }
}

Get information for a list of emails
To get information for a list of emails, send a POST request to:

https://api.sendpulse.com/smtp/emails/info
Request parameter:

Parameter	Type	Description	 
emails	array	List of message ID's, maximum is 500 per request. Example: ["qj7rpf-0q8ru6-ou","qj7rmw-0alndz-r6","qj7rgo-0ejrg3-d2"]	required
Get information about bounces for a 24-hour period
To get information about bounces for a 24-hour period, send a GET request to

https://api.sendpulse.com/smtp/bounces/day
Request parameters:

Parameter	Type	Description	 
date	string	Day you want to get information about. Format: YYYY-MM-DD	optional
limit	int	Number of records	optional
offset	int	Offset (first record to be displayed) 	optional
URL example that gets 10 records starting at 20:

https://api.sendpulse.com/smtp/bounces/day?limit=10&offset=20
You can get information about bounces only for the last 24 hours starting from this moment. For example, today is - 2016-04-28 14:34:00, you will get information about bounces that occured between 2016-04-27 14:34:00 and 2016-04-28 14:34:00.

If the request is successful, you will receive a response:

{
    "total": 1,
    "emails": [
        {
            "email_to": "noreplay@google.com.me.ua",
            "sender": "mail@xn--5ek.bodidron.e.cn.ua",
            "send_date": "2025-07-18 05:01:07",
            "subject": "gggg",
            "smtp_answer_code": 550,
            "smtp_answer_subcode": "5.1.3",
            "smtp_answer_data": "** noreplay@google.com.me.ua MX: NO MX RESP: 5.1.3 No MX or domain found"
        }
    ],
    "request_limit": 100,
    "found": 1
}

Get total number of bounces
To get total number of bounces, send a GET request to:

https://api.sendpulse.com/smtp/bounces/day/total
If the request is successful, you will receive a response:

{
    "total": 3
}

Unsubscribe a recipient
To unsubscribe a recipient, send a POST request to:

https://api.sendpulse.com/smtp/unsubscribe
Request parameter:

Parameter	Type	Description	 
emails	array	Serialized email array	required
Example of emails structure:

[
  {
    "email": "badUser1@mailserver.com",
    "comment": "comment1"
  },
  {
    "email": "badUser2@mailserver.com",
    "comment": "comment2"
  }
]

If the request is successful, you will receive a response:

{
   "result": true
}

Remove an email From the unsubscribed list
To remove an email from the unsubscribed list, send a DELETE request to:

https://api.sendpulse.com/smtp/unsubscribe
Request parameter:

Parameter	Type	Description	 
emails	array	A serialized email array, for example, ["badUser1@mailserver.com","badUser2@mailserver.com"]	required
If the request is successful, you will receive a response:

{
   "result": true
}

Get a list of unsubscribed users
To get a list of unsubscribed users, send a GET request to:

https://api.sendpulse.com/smtp/unsubscribe
Request parameters:

Parameter	Type	Description	 
date	string	The day you would like to get information about. Format: YYYY-MM-DD	optional
limit	int	Number of records	optional
offset	int	Offset (first record to be displayed) 	optional
URL example that gets 10 records starting at 20:

https://api.sendpulse.com/smtp/unsubscribe?limit=10&offset=20
If you enter a specific day, the data will be for 1 day:

https://api.sendpulse.com/smtp/unsubscribe?date=2016-03-05
If the request is successful, you will receive a response:

[  
   {  
      "email":"4090797@gmail.com",
      "unsubscribe_by_link":1,
      "unsubscribe_by_user":0,
      "spam_complaint":1,
      "date":"2018-11-24 19:19:01"
   },
   {  
      "email":"4lik@yahoo.com",
      "unsubscribe_by_link":1,
      "unsubscribe_by_user":0,
      "spam_complaint":1,
      "date":"2019-03-20 16:47:01"
   }
]

Get information about a contact's subscription status
To check if a contact is in your list of unsubscribed users, send a GET request to:

https://api.sendpulse.com/smtp/unsubscribe/search?email={your_contact_email}
URL request parameter:

Parameter	Type	Description	
email	string	Your contact’s email address	required
If the contact is in your list of unsubscribed users, you will receive a response:

{
   "result":true
}

If the contact is not in your list of unsubscribed users, you will receive a response:

{
   "result":false
}

Resubscribe a recipient
To send a resubscription email to a contact, send a POST request to:

https://api.sendpulse.com/smtp/resubscribe
Request parameters:

Parameter	Type	Description	 
email	string	
Recipient's email address 

required
sender	string	
Sender's email address

required
lang	string	
Confirmation email's language. Possible values: ru, en, ua, tr, es, pt. Default: en

optional 
Please note that from one account, you can only send 5 emails with resubscription requests within 24 hours.

Example of the request:

{
    "email":"test@test.com",
    "sender":"my_sender@test.com",
    "lang":"en"
}

If the request is successful, you will receive a response:

{    
    "result": true,
    "id": "pzkic9-0afezp-fc" // Sent email ID 
}

Get a list of sender’s IP addresses
To get a list of sender's IP addresses, send a GET request to:

https://api.sendpulse.com/smtp/ips
If the request is successful, you will receive a response:

[
  "127.0.0.1"
]

Get a list of sender’s email addresses
To get a list of your sender email addresses within the SMTP service, send a GET request to:

https://api.sendpulse.com/smtp/senders
If the request is successful, you will receive a response:

[
    "sender@domain.com"
]

Get a list of allowed domains
To get a list of allowed domains, send a GET request to:

https://api.sendpulse.com/v2/email-service/smtp/sender_domains
Note that the link that allows you to get the list of allowed sending domains has changed on 2023-05-17.

If the request is successful, you will receive a response:

{
    "data": {
        "result": true,
        "data": [
            {
                "id": 37857,
                "user_id": 7043663,
                "service_type": 3,
                "service_value": "example.com",
                "status": 0,
                "expire_date": null,
                "auto_free_prolong": 0,
                "currency": "",
                "is_default": true,
                "ssl_type": 0,
                "ssl_expired": null,
                "ssl_generated": null,
                "checks": {
                    "check_dkim": false,
                    "check_spf": false,
                    "check_dmarc": false,
                    "all_checks": false,
                    "spf_txt_needed": "v=spf1 include:mxsspf.sendpulse.com +a +mx -all"
                }
            }
        ]
    }
}

Response parameters:

Parameter	Type	Description	 
id	int	Sender ID	 
user_id	int	User ID	 
service_type	int	Service type responsible for the sending domain. Always equals 3	 
service_value	string	Domain value	 
status	int	Sending domain status. Takes on the following values:
0 — if the status is inactive;

1 — if the status is active.

 
is_default	boolean	Specifies the default domain	 
ssl_type	int	Certificate type. Takes on the following values:
0 — no SSL,

1 — SendPulse-generated SSL certificate,

2 — user-generated SSL certificate

 
ssl_expired	null|datetime	Time and date till which the user's SSL certificate is valid	 
ssl_generated	null|int	SendPulse SSL certificate generation label	 
checks	object	Domain status validation object	 
Add a sender email
To add a sender, send a POST request to:

https://api.sendpulse.com/senders
SendPulse has merged sender email addresses for the Email and SMTP service. You can use email addresses you add to the Email service after 2023-05-17 in the SMTP service as well.

Request parameters:

Parameters	Type	Description	 
email	string	Sender’s email address	required
name	string	Sender’s name	required
Request example:

{
   "email": "sender@example.com",
   "name": "Sender"
}

If request is successful, you will receive a response:

{
   "result": true
}

Add a domain
To add a domain, send a POST request to:

https://api.sendpulse.com/v2/email-service/smtp/sender_domains/{{mydomain.com}}
Note that the link that allows you to add sending domains has changed on 2023-05-17. SendPulse has also removed the domain verification method.

Request parameter in URL:

Parameter	Type	Description	 
mydomain.com	string	Your sender domain	required
Request Example:

https://api.sendpulse.com/v2/email-service/smtp/sender_domains/mydomain.com
If the request is successful, you will receive a response:

{
    "data": {
        "result": true,
        "error": null
    }
}
