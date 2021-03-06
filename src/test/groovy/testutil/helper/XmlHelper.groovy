package testutil.helper

import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

/**
 * @author Kamill Sokol
 */
class XmlHelper {

    static String getctag(def result) {
        def getctag = new XmlSlurper().parseText(result).response[0].propstat.prop.getctag.text()
        assertThat(getctag, notNullValue())
        return getctag
    }

    static String getetag(def result) {
        return getetag(result, 0)
    }

    static String getetag(def result, def idx) {
        def getetag = new XmlSlurper().parseText(result).response[idx].propstat.prop.getetag.text()
        assertThat(getetag, notNullValue())
        return getetag
    }

    static String existinguuid(def result) {
        def getetag = new XmlSlurper().parseText(result).'no-uid-conflict'.'existing-uuid'.text()
        assertThat(getetag, notNullValue())
        return getetag
    }

    static String conflictinguuid(def result) {
        def getetag = new XmlSlurper().parseText(result).'no-uid-conflict'.'conflicting-uuid'.text()
        assertThat(getetag, notNullValue())
        return getetag
    }
}
