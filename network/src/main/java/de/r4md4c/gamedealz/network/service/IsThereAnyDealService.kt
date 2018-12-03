package de.r4md4c.gamedealz.network.service

import kotlinx.coroutines.Deferred
import retrofit2.http.*

/**
 * A retrofit interface for accessing IsThereAnyDeal's API.
 * Check https://itad.docs.apiary.io/#reference/ for documentation.
 */
interface IsThereAnyDealService {

    @GET("oauth/authorize?response_type=code")
    fun authorize(
        @Query("client_id") clientId: String,
        @Query("value") state: String,
        @Query("scope") scope: String,
        @Query("redirect_uri") redirectUri: String
    ): Deferred<String>

    @FormUrlEncoded
    @POST("oauth/token/")
    fun requestToken(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    )
}