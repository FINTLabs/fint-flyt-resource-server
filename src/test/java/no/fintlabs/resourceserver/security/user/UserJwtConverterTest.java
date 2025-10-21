package no.fintlabs.resourceserver.security.user;

class UserJwtConverterTest {

//    private final InternalApiSecurityProperties properties = Mockito.mock(InternalApiSecurityProperties.class);
//    private final UserJwtConverter converter = new UserJwtConverter("admin", userClaimFormattingService);
//
//    // TODO 16/10/2025 eivindmorch: Rewrite tests
//    @Test
//    void converting_fint_user_jwt_should_result_in_three_authorities() {
//        AbstractAuthenticationToken authenticationToken = converter.convert(JwtFactory.createEndUserJwt()).block();
//        assertNotNull(authenticationToken);
//        assertEquals(3, authenticationToken.getAuthorities().size());
//    }
//
//    @Test
//    void converting_user_jwt_should_remove_backslash_and_quote_from_claims() {
//        Jwt jwt = JwtFactory.createEndUserJwt();
//        Mono<AbstractAuthenticationToken> convertedToken = converter.convert(jwt);
//        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) convertedToken.block();
//        assertNotNull(jwtAuthenticationToken);
//        Jwt modifiedJwt = jwtAuthenticationToken.getToken();
//        modifiedJwt.getClaims().forEach((key, value) -> {
//            if (value instanceof String s) {
//                assertFalse(s.contains("\\"));
//                assertFalse(s.contains("\""));
//            }
//        });
//    }
}
