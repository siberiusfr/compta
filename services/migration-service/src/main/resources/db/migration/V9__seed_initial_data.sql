-- V9__seed_initial_data.sql
-- Seed initial reference data

-- Insert Roles
INSERT INTO roles (name, code, description) VALUES
('Administrateur', 'ADMIN', 'Administrateur système avec tous les privilèges'),
('Comptable', 'COMPTABLE', 'Comptable gérant plusieurs sociétés'),
('Société', 'SOCIETE', 'Représentant d''une société'),
('Employé', 'EMPLOYEE', 'Employé avec accès limité')
ON CONFLICT (code) DO NOTHING;

-- Insert Permissions
INSERT INTO permissions (code, resource, action, description) VALUES
-- User Management
('USERS:READ', 'USERS', 'READ', 'Voir les utilisateurs'),
('USERS:WRITE', 'USERS', 'WRITE', 'Créer/modifier les utilisateurs'),
('USERS:DELETE', 'USERS', 'DELETE', 'Supprimer les utilisateurs'),

-- Company Management
('COMPANIES:READ', 'COMPANIES', 'READ', 'Voir les sociétés'),
('COMPANIES:WRITE', 'COMPANIES', 'WRITE', 'Créer/modifier les sociétés'),
('COMPANIES:DELETE', 'COMPANIES', 'DELETE', 'Supprimer les sociétés'),

-- Accounting
('ACCOUNTING:READ', 'ACCOUNTING', 'READ', 'Consulter la comptabilité'),
('ACCOUNTING:WRITE', 'ACCOUNTING', 'WRITE', 'Créer/modifier des écritures'),
('ACCOUNTING:VALIDATE', 'ACCOUNTING', 'VALIDATE', 'Valider des écritures'),
('ACCOUNTING:CLOSE', 'ACCOUNTING', 'CLOSE', 'Clôturer un exercice'),

-- Documents
('DOCUMENTS:READ', 'DOCUMENTS', 'READ', 'Consulter les documents'),
('DOCUMENTS:WRITE', 'DOCUMENTS', 'WRITE', 'Créer/modifier des documents'),
('DOCUMENTS:DELETE', 'DOCUMENTS', 'DELETE', 'Supprimer des documents'),
('DOCUMENTS:SHARE', 'DOCUMENTS', 'SHARE', 'Partager des documents'),

-- HR
('EMPLOYEES:READ', 'EMPLOYEES', 'READ', 'Voir les employés'),
('EMPLOYEES:WRITE', 'EMPLOYEES', 'WRITE', 'Créer/modifier les employés'),
('EMPLOYEES:DELETE', 'EMPLOYEES', 'DELETE', 'Supprimer les employés'),

('PAYSLIPS:READ', 'PAYSLIPS', 'READ', 'Consulter les fiches de paie'),
('PAYSLIPS:WRITE', 'PAYSLIPS', 'WRITE', 'Créer/modifier les fiches de paie'),
('PAYSLIPS:VALIDATE', 'PAYSLIPS', 'VALIDATE', 'Valider les fiches de paie'),

('LEAVES:READ', 'LEAVES', 'READ', 'Voir les congés'),
('LEAVES:WRITE', 'LEAVES', 'WRITE', 'Demander des congés'),
('LEAVES:APPROVE', 'LEAVES', 'APPROVE', 'Approuver des congés'),

('ATTENDANCES:READ', 'ATTENDANCES', 'READ', 'Voir les présences'),
('ATTENDANCES:WRITE', 'ATTENDANCES', 'WRITE', 'Gérer les présences'),

-- Reports
('REPORTS:READ', 'REPORTS', 'READ', 'Consulter les rapports'),
('REPORTS:EXPORT', 'REPORTS', 'EXPORT', 'Exporter les rapports')
ON CONFLICT (code) DO NOTHING;

-- Assign permissions to ADMIN role (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Assign permissions to COMPTABLE role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'COMPTABLE'
AND p.code IN (
    'COMPANIES:READ',
    'ACCOUNTING:READ', 'ACCOUNTING:WRITE', 'ACCOUNTING:VALIDATE', 'ACCOUNTING:CLOSE',
    'DOCUMENTS:READ', 'DOCUMENTS:WRITE', 'DOCUMENTS:SHARE',
    'EMPLOYEES:READ', 'EMPLOYEES:WRITE',
    'PAYSLIPS:READ', 'PAYSLIPS:WRITE', 'PAYSLIPS:VALIDATE',
    'LEAVES:READ', 'LEAVES:APPROVE',
    'ATTENDANCES:READ', 'ATTENDANCES:WRITE',
    'REPORTS:READ', 'REPORTS:EXPORT'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Assign permissions to SOCIETE role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SOCIETE'
AND p.code IN (
    'ACCOUNTING:READ',
    'DOCUMENTS:READ', 'DOCUMENTS:WRITE',
    'EMPLOYEES:READ', 'EMPLOYEES:WRITE',
    'PAYSLIPS:READ', 'PAYSLIPS:WRITE',
    'LEAVES:READ', 'LEAVES:APPROVE',
    'ATTENDANCES:READ',
    'REPORTS:READ', 'REPORTS:EXPORT'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Assign permissions to EMPLOYEE role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'EMPLOYEE'
AND p.code IN (
    'PAYSLIPS:READ',
    'LEAVES:READ', 'LEAVES:WRITE',
    'ATTENDANCES:READ',
    'DOCUMENTS:READ'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Insert Leave Types (Tunisia standard)
INSERT INTO leave_types (company_id, name, code, days_per_year, is_paid, requires_approval, description) VALUES
(1, 'Congé annuel', 'ANNUAL', 15, true, true, 'Congé annuel payé'),
(1, 'Congé maladie', 'SICK', 30, true, false, 'Congé maladie avec certificat médical'),
(1, 'Congé maternité', 'MATERNITY', 30, true, false, 'Congé de maternité'),
(1, 'Congé paternité', 'PATERNITY', 1, true, false, 'Congé de paternité'),
(1, 'Congé sans solde', 'UNPAID', 0, false, true, 'Congé sans solde')
ON CONFLICT (company_id, code) DO NOTHING;

-- Insert Notification Templates
INSERT INTO notification_templates (name, template_type, code, subject_template, body_template, variables, language) VALUES
(
    'Bienvenue',
    'EMAIL',
    'USER_WELCOME',
    'Bienvenue sur COMPTA ERP',
    '<h1>Bienvenue {{firstName}} {{lastName}}</h1>
    <p>Votre compte a été créé avec succès.</p>
    <p>Email: {{email}}</p>
    <p>Vous pouvez vous connecter dès maintenant.</p>',
    '{"firstName": "Prénom de l''utilisateur", "lastName": "Nom de l''utilisateur", "email": "Email de l''utilisateur"}',
    'fr'
),
(
    'Demande de congé soumise',
    'EMAIL',
    'LEAVE_REQUEST_SUBMITTED',
    'Demande de congé soumise',
    '<h2>Demande de congé</h2>
    <p>Bonjour {{employeeName}},</p>
    <p>Votre demande de congé a été soumise avec succès.</p>
    <p><strong>Type:</strong> {{leaveType}}</p>
    <p><strong>Du:</strong> {{startDate}}</p>
    <p><strong>Au:</strong> {{endDate}}</p>
    <p><strong>Nombre de jours:</strong> {{daysCount}}</p>
    <p>Vous serez notifié une fois la demande traitée.</p>',
    '{"employeeName": "Nom de l''employé", "leaveType": "Type de congé", "startDate": "Date de début", "endDate": "Date de fin", "daysCount": "Nombre de jours"}',
    'fr'
),
(
    'Congé approuvé',
    'EMAIL',
    'LEAVE_APPROVED',
    'Votre demande de congé a été approuvée',
    '<h2>Demande approuvée</h2>
    <p>Bonjour {{employeeName}},</p>
    <p>Votre demande de congé a été <strong>approuvée</strong>.</p>
    <p><strong>Type:</strong> {{leaveType}}</p>
    <p><strong>Du:</strong> {{startDate}}</p>
    <p><strong>Au:</strong> {{endDate}}</p>
    <p>Commentaire: {{comment}}</p>',
    '{"employeeName": "Nom de l''employé", "leaveType": "Type de congé", "startDate": "Date de début", "endDate": "Date de fin", "comment": "Commentaire"}',
    'fr'
),
(
    'Congé rejeté',
    'EMAIL',
    'LEAVE_REJECTED',
    'Votre demande de congé a été rejetée',
    '<h2>Demande rejetée</h2>
    <p>Bonjour {{employeeName}},</p>
    <p>Votre demande de congé a été <strong>rejetée</strong>.</p>
    <p><strong>Type:</strong> {{leaveType}}</p>
    <p><strong>Du:</strong> {{startDate}}</p>
    <p><strong>Au:</strong> {{endDate}}</p>
    <p>Raison: {{reason}}</p>',
    '{"employeeName": "Nom de l''employé", "leaveType": "Type de congé", "startDate": "Date de début", "endDate": "Date de fin", "reason": "Raison du rejet"}',
    'fr'
),
(
    'Fiche de paie disponible',
    'EMAIL',
    'PAYSLIP_READY',
    'Votre fiche de paie est disponible',
    '<h2>Fiche de paie</h2>
    <p>Bonjour {{employeeName}},</p>
    <p>Votre fiche de paie pour la période du <strong>{{periodStart}}</strong> au <strong>{{periodEnd}}</strong> est disponible.</p>
    <p><strong>Salaire net:</strong> {{netSalary}} TND</p>
    <p>Vous pouvez la consulter et la télécharger depuis votre espace.</p>',
    '{"employeeName": "Nom de l''employé", "periodStart": "Début de période", "periodEnd": "Fin de période", "netSalary": "Salaire net"}',
    'fr'
),
(
    'Réinitialisation mot de passe',
    'EMAIL',
    'PASSWORD_RESET',
    'Réinitialisation de votre mot de passe',
    '<h2>Réinitialisation mot de passe</h2>
    <p>Bonjour {{firstName}},</p>
    <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
    <p>Cliquez sur le lien ci-dessous pour créer un nouveau mot de passe:</p>
    <p><a href="{{resetLink}}">Réinitialiser mon mot de passe</a></p>
    <p>Ce lien expire dans 1 heure.</p>
    <p>Si vous n''avez pas demandé cette réinitialisation, ignorez cet email.</p>',
    '{"firstName": "Prénom", "resetLink": "Lien de réinitialisation"}',
    'fr'
)
ON CONFLICT (code) DO NOTHING;

-- Create a default company (for testing/demo purposes)
INSERT INTO companies (name, legal_name, tax_id, address, city, country, is_active) VALUES
('Demo Company', 'Demo Company SARL', '0000000A', '123 Avenue Habib Bourguiba', 'Tunis', 'Tunisia', true)
ON CONFLICT (tax_id) DO NOTHING;

COMMENT ON DATABASE compta_db IS 'COMPTA ERP Database - Initial data seeded';
