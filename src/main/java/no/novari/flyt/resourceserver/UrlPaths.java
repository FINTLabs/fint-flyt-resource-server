package no.novari.flyt.resourceserver;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlPaths {
    public static final String INTERNAL_ADMIN_API = "/api/intern/admin";
    public static final String INTERNAL_API = "/api/intern";
    public static final String INTERNAL_CLIENT_API = "/api/intern-klient";
    public static final String EXTERNAL_API = "/api";
}
