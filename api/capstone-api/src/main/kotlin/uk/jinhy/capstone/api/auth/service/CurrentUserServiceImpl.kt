package uk.jinhy.capstone.api.auth.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.infra.security.CurrentUserService

@Service
class CurrentUserServiceImpl(
    private val userRepository: UserRepository,
) : CurrentUserService {

    @Transactional(readOnly = true)
    override fun loadUser(id: String): Any? {
        val user = userRepository.findUserById(id)
        return if (user != null && user.isActive) {
            user
        } else {
            null
        }
    }
}
