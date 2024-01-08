package no.fintlabs.resourceserver.security

import no.fintlabs.resourceserver.security.client.FintFlytJwtUserConverter
import no.fintlabs.resourceserver.testutils.JwtFactory
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import reactor.core.publisher.Mono
import spock.lang.Specification

class FintFlytJwtUserConverterSpec extends Specification {

    FintFlytJwtUserConverter converter = new FintFlytJwtUserConverter()

    def "Converting a FINT user JWT should result in 3 authorities"() {
        when:
        def authenticationToken = converter.convert(JwtFactory.createEndUserJwt()).block()

        then:
        authenticationToken.getAuthorities().size() == 3
    }

    def "Converting a FINT Flyt user jwt should remove \\ and \" from claims"() {
        given: "A JWT token with illegal characters in the claims"
        Jwt jwt = JwtFactory.createEndUserJwt()

        when: "We convert the token"
        Mono<AbstractAuthenticationToken> convertedToken = converter.convert(jwt)

        then: "The illegal characters should be removed from all claims"
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) convertedToken.block()
        Jwt modifiedJwt = jwtAuthenticationToken.getToken()
        modifiedJwt.getClaims().each { key, value ->
            assert !(value instanceof String && ((String)value).contains("\\"))
            assert !(value instanceof String && ((String)value).contains("\""))
        }
    }

}
