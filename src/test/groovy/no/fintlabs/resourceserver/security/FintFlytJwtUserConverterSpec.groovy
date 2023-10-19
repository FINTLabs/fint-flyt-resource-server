package no.fintlabs.resourceserver.security

import no.fintlabs.resourceserver.security.client.FintFlytJwtUserConverter
import no.fintlabs.resourceserver.testutils.JwtFactory
import spock.lang.Specification

class FintFlytJwtUserConverterSpec extends Specification {

    def "Converting a FINT user JWT should result in 5 authorities"() {
        given:
        def converter = new FintFlytJwtUserConverter()

        when:
        def authenticationToken = converter.convert(JwtFactory.createEndUserJwt()).block()

        then:
        authenticationToken.getAuthorities().size() == 5
    }

//    def "Converting a FINT Flyt user jwt should remove \\ and \" from organizationnumber and organizationid"() {
//        given:
//        def converter = new FintFlytJwtUserConverter()
//
//        def jwt = JwtFactory.createEndUserJwt().mutate() { builder ->
//            builder.claim("organizationnumber", "\"123456789\"")
//            builder.claim("organizationid", "\"test.com\"")
//        }.build()
//
//        when:
//        def authenticationToken = converter.convert(jwt).block()
//
//        then:
//        authenticationToken.principal.organizationnumber == "123456789"
//        authenticationToken.principal.organizationid == "test.com"
//    }
}
