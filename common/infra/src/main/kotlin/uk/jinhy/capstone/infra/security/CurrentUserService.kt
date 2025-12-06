package uk.jinhy.capstone.infra.security

interface CurrentUserService {
    fun loadUser(id: String): Any?
}
