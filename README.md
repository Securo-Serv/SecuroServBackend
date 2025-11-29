# SecuroServ

**SecuroServ** is a secure file storage and access-control backend designed to protect sensitive user files.  
It provides encrypted file handling, strict authentication, and role-based Storage control, ensuring that uploaded files remain confidential and accessible only to authorized users.

---

## ✨ Features

- **Secure File Upload:** Users can upload files safely with validations.
- **Protected File Retrieval:** Only authorized users can download files.
- **Encrypted Storage:** Files are stored with hashed/obfuscated filenames.
- **JWT Authentication:** Secure login and authorization for all routes.
- **Role-Based Storage Control:** Strorage rules based on user or premium roles.
- **Structured Security Workflow:** Authentication → Authorization → Validation → Access.
- **Razorpay Payment Integration:** Supports secure payments for premium storage or file operations.
- **Layered Architecture:** Controller → Service → Repository → Entity → Security.
- **Exception Handling:** Clean error responses for invalid and unauthorized actions.-

---

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Security (JWT)**
- **Spring Data JPA**
- **PostgreSQL**
- **Hibernate**
- **Maven**

---

## 🧩 Project Architecture

 ### **1. Controller Layer**
 Handles API requests for authentication and file operations.

 ### **2. Service Layer**
 Responsible for:
 - File Encryption logic
 - File storage logic  
 - Access validation  
 - JWT verification  
 - Metadata handling  

 ### **3. Repository Layer**
 Database operations for:
 - Users  
 - File metadata  

 ### **4. Entity Layer**
 Defines structured database models.

 ### **5. Security Layer**
 Implements:
 - JWT token generation & validation  
 - Authentication filters  
 - Route protection  

 ### **6. Exception Handling**
 Provides global exception handling for all API endpoints.

---

## 🔄 Workflow Overview

1. User logs in → receives JWT  
2. User uploads a file  
3. System stores file securely  
4. Metadata saved in DB  
5. Authorized users can request file download  
6. JWT validates access  

---

## 🏁 Getting Started (Setup Instructions)

## 1. Clone the repository
git clone https://github.com/your-username/your-repo.git
cd your-repo

## 2. Configure MySQL
spring.datasource.url=jdbc:postgresql://localhost:5432/yourdbname
spring.datasource.username=postgres
spring.datasource.password=1234

## 3. Install dependencies
mvn clean install

## 4. Run application
mvn spring-boot:run


### 🔐 Auth APIs
- **POST `/auth/register`** — Register a new user
- **POST `/auth/login`** — Authenticate user and return JWT token

---

### 📁 File APIs
- **POST `/files/upload`**  
  Upload a file securely.  
  - Validates file type & size  
  - Encrypts the file  
  - Stores encrypted file  
  - Saves metadata in DB  

- **GET `/files/{id}`**  
  Download a file.  
  - Fetches encrypted file  
  - Decrypts it before sending  
  - Only accessible to authorized users  

- **DELETE `/files/{id}`**  
  Delete a file (role/access based)

---

### 🔐 Encryption Flow (Internal)
> No separate API — encryption & decryption happen automatically in upload/download services.

---

### 🗂 Storage Management APIs
- **GET `/storage/usage/{userId}`** — Returns how much storage the user has used  
- **GET `/storage/limit/{userId}`** — Returns the user's storage limit (if implemented)

---

### 💳 Payment APIs (Razorpay)
- **POST `/payments/create-order`** — Create Razorpay order for premium storage or file operations  
- **POST `/payments/verify`** — Verify Razorpay payment signature  

---

### ⚠️ Error Handling
Unified error responses with:
- Proper HTTP status codes
- Validation errors
- Storage limit errors
- Unauthorized / forbidden access

---

## 📁 Folder Structure

```
src/
└── main/
    ├── java/com/securoserv/
    │   ├── Configuration/         # App configuration (CORS, Beans, etc.)
    │   ├── Controller/            # Handles API requests (auth, files, payments, storage)
    │   ├── DTO/                   # Request & response DTOs
    │   ├── Entity/                # Database models (User, FileMetadata, etc.)
    │   ├── ExceptionHandling/     # Global exception handling
    │   ├── External/              # External integrations (Razorpay, etc.)
    │   ├── Mapper/                # Entity <-> DTO mappers
    │   ├── Repository/            # Spring Data JPA repositories
    │   ├── Security/              # JWT auth, filters, role-based access
    │   └── Service/               # Business logic (encryption, storage, payments)
    │
    └── resources/
        ├── application.properties
        └── static/                # (Optional) static assets
```

  
