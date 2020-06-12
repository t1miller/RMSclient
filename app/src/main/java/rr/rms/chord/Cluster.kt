package rr.rms.chord

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.math.pow


/**
 *  A Cluster:
 *  - Is a Chord Node https://en.wikipedia.org/wiki/Chord_(peer-to-peer)
 *  - Has at least one member Node
 *  - Acts as one addressable cohesive server
 *  - Can communicate to its member Nodes
 *  - Stores key(s)
 *  - Has a consensus protocol implementation (quorum)
 *  - Has a socket that responds to ip-address
 *  - Has the ability to lookup all other internet enabled clusters in its chord network
 */
class Cluster {

    private var nodeId: Long = getChordId()

    companion object {
        /**
         *  Parameter that controls hash table collision rate.
         *  Max Network size is 2^M
         */
        const val M = 20

        /**
         *  Parameter that controls the failure rate of a request.
         *  R is the amount of successors and predecessors to keep
         *  track of in case one fails.
         *   - Small R, less network noise, higher fail rate
         *   - Big R, more network noise, lower fail rate
         */
        const val R = 10
    }


//    private var predecessor: Cluster? = null
//
//    /** The finger table. finger[0] = successor node */
//    private val finger: Array<Cluster?> = Array(10){null}
//
//    private val start = finger.size

    fun join(id: String?) {

    }

    /**
     *  Initialize finger table of local node
     *  @param id is an arbitrary node already in the network
     */
    fun initFingerTable(node: Cluster) {

    }

    private fun getChordId(): Long {
        // todo hash should be from a real ip
        val input = UUID.randomUUID().toString()

        val md = MessageDigest.getInstance("SHA-1")
        md.reset()
        md.update(input.toByteArray())

        var hash = BigInteger(md.digest())
        hash = hash.abs()
        hash = hash.mod(BigDecimal.valueOf(2.0.pow(M)).toBigInteger())

        Timber.d("chordId: %s", hash)

        return hash.toLong()
    }
}

