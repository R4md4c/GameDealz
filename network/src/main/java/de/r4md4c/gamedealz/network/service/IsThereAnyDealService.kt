package de.r4md4c.gamedealz.network.service

import de.r4md4c.gamedealz.network.BuildConfig
import de.r4md4c.gamedealz.network.model.*
import kotlinx.coroutines.Deferred
import retrofit2.http.*

typealias RegionCodes = Map<String, Region>
typealias ShopPlains = Map<String, IdToPlainMap>
typealias PlainPriceList = Map<String, ListWrapper<Price>>
/**
 * A retrofit interface for accessing IsThereAnyDeal's API.
 * Check https://itad.docs.apiary.io/#reference/ for documentation.
 */
interface IsThereAnyDealService {

    @GET("oauth/authorize?response_type=code")
    fun authorize(
        @Query("client_id") clientId: String = BuildConfig.CLIENT_ID,
        @Query("value") state: String,
        @Query("scope") scope: String,
        @Query("redirect_uri") redirectUri: String
    ): Deferred<String>

    @FormUrlEncoded
    @POST("oauth/token/")
    fun requestToken(
        @Field("code") code: String,
        @Field("grant_type") grantType: String = GRANT_TYPE,
        @Field("client_id") clientId: String = BuildConfig.CLIENT_ID,
        @Field("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET
    )

    @GET("v01/web/regions/")
    fun regions(): Deferred<DataWrapper<RegionCodes>>

    @GET("v02/web/stores")
    fun stores(@Query("region") region: String, @Query("country") country: String): Deferred<Stores>

    @GET("v02/game/plain")
    fun plain(
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("shop") shop: String,
        @Query("game_id") gameId: String
    ): Deferred<DataWrapper<Plain>>

    @GET("v01/game/plain/list")
    fun allPlains(
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("shops") shops: Set<String>
    ): Deferred<DataWrapper<ShopPlains>>

    @GET("v01/game/prices")
    fun prices(
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("plains") plains: Set<String>,
        @Query("region") region: String? = null,
        @Query("country") country: String? = null,
        @Query("shops") shops: Set<String>? = null
    )
            : Deferred<DataWrapper<PlainPriceList>>
}

private const val GRANT_TYPE = "authorization_code"
