package blue.starry.saya.services.comments.twitter

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.token
import blue.starry.saya.Env

val twitter = PenicillinClient {
    account {
        application(Env.TWITTER_CK, Env.TWITTER_CS)
        token(Env.TWITTER_AT, Env.TWITTER_ATS)
    }
}
