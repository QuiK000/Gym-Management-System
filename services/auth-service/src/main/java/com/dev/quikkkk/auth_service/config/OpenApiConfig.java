package com.dev.quikkkk.auth_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gym Management System - Authentication Service API",
                version = "1.0.0",
                description = """
                        # Authentication Service API Documentation
                        
                                                This service handles all authentication and authorization operations for the Gym Management System.
                        
                                                ## Features
                                                * User registration with email verification
                                                * Login with JWT tokens (Access + Refresh)
                                                * Password reset functionality
                                                * Token refresh mechanism
                                                * Brute force protection
                                                * Rate limiting
                        
                                                ## Authentication Flow
                                                1. Register a new account (`POST /api/v1/auth/register`)
                                                2. Verify email with code (`POST /api/v1/auth/verify-email`)
                                                3. Login to get tokens (`POST /api/v1/auth/login`)
                                                4. Use access token for authenticated requests
                                                5. Refresh tokens when expired (`POST /api/v1/auth/refresh-token`)
                        
                                                ## Rate Limits
                                                * Registration: 3 attempts per IP per hour
                                                * Login: 5 attempts per IP per 15 minutes
                                                * Email verification: 3 codes per email per hour
                        
                                                ## Support
                                                For issues or questions, contact support@quikkkk.dev
                        """,
                contact = @Contact(
                        name = "Gym Management System Support",
                        email = "support@quikkkk.dev",
                        url = "https://github.com/QuiK000/Gym-Management-System"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development",
                        url = "http://localhost:8090"
                ),
                @Server(
                        description = "Gateway",
                        url = "http://localhost:8222"
                ),
                @Server(
                        description = "Production",
                        url = "https://api.quikkkk.dev"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = """
                JWT Bearer token authentication.
                
                                To authenticate:
                                1. Login via `/api/v1/auth/login`
                                2. Copy the `access_token` from response
                                3. Click 'Authorize' button above
                                4. Enter: `Bearer YOUR_ACCESS_TOKEN`
                
                                Token expires in 24 hours. Use refresh token to get a new one.
                """
)
@SecurityScheme(
        name = "Refresh Token",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER,
        description = "Refresh token for obtaining new access tokens"
)
public class OpenApiConfig {
}
