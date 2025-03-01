package dev.whysoezzy.services

import dev.whysoezzy.data.model.User
import dev.whysoezzy.data.model.UserProfile
import dev.whysoezzy.data.model.UserRequest
import dev.whysoezzy.data.repositories.UsersRepository

class UsersService(
    private val usersRepository: UsersRepository
) {

    fun getUserById(id: String): User? {
        return usersRepository.getUserById(id)
    }

    fun getUserProfileById(id: String): UserProfile? {
        return usersRepository.getUserProfileById(id)
    }

    fun getUserByPhone(phoneNumber: String): User? {
        return usersRepository.getUserByPhone(phoneNumber)
    }

    fun createUser(phoneNumber: String, request: UserRequest): User {
        return usersRepository.createUser(phoneNumber, request)
    }

    fun updateUser(id: String, request: UserRequest): User? {
        return usersRepository.updateUser(id, request)
    }

    fun deleteUser(id: String): Boolean {
        return usersRepository.deleteUser(id)
    }
}