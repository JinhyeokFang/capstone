package uk.jinhy.capstone.util.test.fixture

object TestFixture {
    fun createRandomString(length: Int = 10): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    fun createRandomEmail(domain: String = "test.com"): String {
        return "${createRandomString(8)}@$domain"
    }

    fun createRandomPhoneNumber(): String {
        val first = listOf("010", "011", "016", "017", "018", "019").random()
        val second = (1000..9999).random()
        val third = (1000..9999).random()
        return "$first-$second-$third"
    }
}
