# School Management Application Setup

## Steps to Run the Application

1. **Start Services**
   - Run the `run_all.sh` file.
   - This will build and start all the services.
   - Eureka will register all the APIs.

2. **API Gateway**
   - The API Gateway will be used to call all other services.
   - Configuration is managed in the `configuration.yml` file.
   - Some endpoints are open; you can add required endpoints that should be skipped from `JWTFilter` and authentication.

3. **Common Library**
   - The `common-library` project is a shared library.
   - It contains common configuration files, including profile `.yml`.

4. **UI Project**
   - The UI project has a proxy configuration in the parent directory.
   - It listens to URLs starting with `/rest` and redirects them to the backend service running on port **8000**.
   - If the backend service port changes, update the UI `proxy.conf.json` configuration to redirect requests to the new URL.

---
✅ With these steps, your School Management application will be up and running smoothly.
