package uk.ac.tees.mad.e4615842.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import uk.ac.tees.mad.e4615842.model.HiveRequest
import uk.ac.tees.mad.e4615842.model.HiveResponse

interface ApiService {

    @POST("hive/ai-generated-and-deepfake-content-detection")
    suspend fun detectImage(
        @Header("Authorization") token: String,
        @Body request: HiveRequest
    ): Response<HiveResponse>
}