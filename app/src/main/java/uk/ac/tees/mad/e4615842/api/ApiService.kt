package uk.ac.tees.mad.e4615842.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import uk.ac.tees.mad.e4615842.model.HiveResponse
import retrofit2.http.Headers
import uk.ac.tees.mad.e4615842.model.HiveRequest

data class RegisterRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class ApiResponse(val success: Boolean, val message: String)

interface ApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse>

    @Headers("Authorization: t4q3TNWQbXfvPheEnmb3LQ==")
    @POST("ai-generated-content-detection")
    suspend fun detectImage(
        @Header("Authorization") token: String,
        @Body request: HiveRequest
    ): Response<HiveResponse>
}