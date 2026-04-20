package uk.ac.tees.mad.e4615842.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import uk.ac.tees.mad.e4615842.model.HiveResponse
import uk.ac.tees.mad.e4615842.model.HiveRequest

interface ApiService {

    @POST("ai-generated-content-detection")
    suspend fun detectImage(
        @Header("Authorization") token: String,
        @Body request: HiveRequest
    ): Response<HiveResponse>
}