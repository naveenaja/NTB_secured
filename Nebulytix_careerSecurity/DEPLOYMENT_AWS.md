# AWS Deployment Guide (React Frontend + Spring Boot API)

This guide deploys the existing Spring Boot backend and React frontend to AWS with **four domains**:

- `app.example.com` (main web app)
- `admin.example.com` (admin portal)
- `hr.example.com` (HR portal)
- `payslips.example.com` (payslip workspace)
- `api.example.com` (shared backend API)

## 1) High-level Architecture

- **Frontend**: React apps hosted in **S3 + CloudFront** (separate bucket and CloudFront distribution per domain).
- **Backend**: Spring Boot API hosted in **ECS Fargate** (recommended) or **EC2** behind an **Application Load Balancer (ALB)**.
- **Database**: **Amazon RDS MySQL**.
- **File storage**: **Cloudinary** for resumes and documents.
- **DNS**: **Route 53** with A/AAAA alias records to CloudFront and ALB.

## 2) Domains and DNS (Route 53)

Create hosted zone for `example.com`, then create records:

- `app.example.com` -> CloudFront distribution for main React app
- `admin.example.com` -> CloudFront distribution for admin React app
- `hr.example.com` -> CloudFront distribution for HR React app
- `payslips.example.com` -> CloudFront distribution for payslip React app
- `api.example.com` -> ALB DNS (alias)

## 3) Backend (Spring Boot) on ECS Fargate

### 3.1 Build & containerize

Create a `Dockerfile` (if not present) and build:

```
./mvnw clean package -DskipTests
```

Then build container:

```
docker build -t nebulytix-backend:latest .
```

Push to ECR and use an ECS Fargate task definition.

### 3.2 ECS Task Env Vars

Set these as **ECS task environment variables** or from AWS Secrets Manager:

- `SPRING_DATASOURCE_URL=jdbc:mysql://<rds-endpoint>:3306/securityNT?createDatabaseIfNotExist=true`
- `SPRING_DATASOURCE_USERNAME=<db-user>`
- `SPRING_DATASOURCE_PASSWORD=<db-pass>`
- `SPRING_MAIL_USERNAME=<smtp-username>`
- `SPRING_MAIL_PASSWORD=<smtp-password>`
- `JWT_SECRET=<base64-secret>`
- `JWT_ACCESS_EXPIRATION=900000`
- `JWT_REFRESH_EXPIRATION=604800000`
- `CLOUDINARY_CLOUD_NAME=<cloud-name>`
- `CLOUDINARY_API_KEY=<api-key>`
- `CLOUDINARY_API_SECRET=<api-secret>`

### 3.3 ALB + HTTPS

- Create an ALB with HTTPS listener (port 443) and attach an ACM certificate for `api.example.com`.
- Target group should route to your ECS task container port (default `5054`).

## 4) React Frontend (S3 + CloudFront)

Create **four separate React builds** (or one app with routing if you prefer), then upload each build to its own S3 bucket:

- `app.example.com` -> `s3://nebulytix-app-web`
- `admin.example.com` -> `s3://nebulytix-admin-web`
- `hr.example.com` -> `s3://nebulytix-hr-web`
- `payslips.example.com` -> `s3://nebulytix-payslips-web`

Then create CloudFront distributions pointing to each bucket (origin set to S3). Attach ACM certificates for each subdomain.

## 5) CORS + Cookie Strategy

Since all frontends talk to `api.example.com`, update CORS to allow:

- `https://app.example.com`
- `https://admin.example.com`
- `https://hr.example.com`
- `https://payslips.example.com`

If you use **refresh-token cookies** across domains, configure:

- `Secure=true`
- `SameSite=None`
- `Domain=.example.com`

This enables sharing cookies across subdomains.

## 6) Cloudinary for Resumes & Docs

Instead of local storage paths, store resumes and documents using Cloudinary. Example flow:

- Upload resume file to Cloudinary
- Store returned URL in DB
- Serve/download via Cloudinary URL

Use AWS Secrets Manager or ECS env vars for Cloudinary credentials.

## 7) Production Checklist

- ✅ Use environment variables for SMTP, DB, JWT secret
- ✅ Remove Windows file paths from config
- ✅ Consolidate duplicate CORS configs
- ✅ Use HTTPS everywhere (CloudFront + ALB)
- ✅ Store secrets in AWS Secrets Manager

## 8) Optional: Separate Payslip Backend

If the manager requires strict isolation for payslips:

- Deploy another Spring Boot service (`payslips-api.example.com`)
- Share auth with main API or validate JWT tokens using same secret
- Restrict DB access to payslip tables only

---

If you want, I can add the exact **Nginx/ALB rules**, **Terraform**, or **CloudFormation** templates for this setup.
