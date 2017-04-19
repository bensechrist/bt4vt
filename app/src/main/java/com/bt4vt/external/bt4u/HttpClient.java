package com.bt4vt.external.bt4u;

import com.google.inject.Singleton;

import okhttp3.OkHttpClient;

/**
 * HTTP client used for sending requests to BT4U.
 *
 * @author Ben Sechrist
 */
@Singleton
class HttpClient extends OkHttpClient {
}
