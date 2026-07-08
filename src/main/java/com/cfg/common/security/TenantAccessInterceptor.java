package com.cfg.common.security;

import com.cfg.common.exception.TenantAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

/**
 * Empeche un utilisateur (tout role sauf SUPER_ADMIN) d'acceder aux donnees d'un
 * restaurant autre que le sien, simplement en changeant le {restaurantId} dans l'URL.
 * Les @PreAuthorize sur les controllers ne verifient que le role, jamais l'appartenance.
 */
public class TenantAccessInterceptor implements HandlerInterceptor {

    @SuppressWarnings("unchecked")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Object principalObj = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;

        if (!(principalObj instanceof UserPrincipal principal)) {
            return true; // endpoints publics (login, etc.) : rien a verifier ici
        }

        if ("SUPER_ADMIN".equals(principal.getRole())) {
            return true; // acces plateforme complet, legitime
        }

        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String pathRestaurantId = pathVariables != null ? pathVariables.get("restaurantId") : null;

        if (pathRestaurantId == null) {
            return true; // route sans restaurantId dans le chemin
        }

        UUID requested;
        try {
            requested = UUID.fromString(pathRestaurantId);
        } catch (IllegalArgumentException e) {
            return true; // format invalide, laisse le controller/la validation le gerer
        }

        if (!requested.equals(principal.getRestaurantId())) {
            throw new TenantAccessException("Ce compte n'a pas accès à ce restaurant");
        }

        return true;
    }
}
