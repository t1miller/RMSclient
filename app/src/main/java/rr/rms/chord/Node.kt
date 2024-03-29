package rr.rms.chord

import rr.rms.cache.BlockCache
import rr.rms.wifiaware.library.net.NetworkUtils
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.math.pow

/**
 *  A Node:
 *  - Is a Chord Node https://en.wikipedia.org/wiki/Chord_(peer-to-peer)
 *  - Has at least one member Node
 *  - Acts as one addressable cohesive server
 *  - Can communicate to its member Nodes
 *  - Stores key(s)
 *  - Has a consensus protocol implementation (quorum)
 *  - Has a socket that responds to ip-address
 *  - Has the ability to lookup all other internet enabled clusters in its chord network
 */
class Node {

    // todo hash should be from a real ip
    var nodeId: Long = getChordId()

    private var finger: Array<Node?> = emptyArray()

    var predecessor: Node? = null

    var successor: Node? = null

    var hasInternet: Boolean? = null

    var cache: BlockCache? = null

    constructor()

    constructor(hasInternet: Boolean?, nodeId: Long) {
        this.nodeId = nodeId
        this.hasInternet = hasInternet
    }

    /** keep track of finger index for maintenance */
    private var next: Int = 0

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

    private fun create() {
        finger = Array(M){ Node() }
        predecessor = null
        successor = null
    }

    private fun join(node: Node) {
        predecessor = null
        successor = node.findSuccessor(nodeId)
    }

    /**
     * Called periodically.
     */
    private fun stabilize() {
        successor?.let {
            val x = it.predecessor
            if (x?.nodeId in nodeId+1 until it.nodeId) {
                successor = x
            }
            it.notify(this)
        }
    }

    /**
     * nprime thinks it might be our predecessor
     */
    private fun notify(nprime: Node) {
        if (predecessor == null || predecessor?.nodeId!! < nprime.nodeId && nprime.nodeId < nodeId ){
            predecessor = nprime
        }
    }

    /**
     * Called periodically. Refreshes finger table entries
     */
    fun fixFingers() {
        if (next >= M) {
            next = 0
        }
        finger[next] = findSuccessor(nodeId + 2.0.pow(next-1).toLong())
        next += 1
    }

    /**
     * Called periodically. Checks whether predecessor has failed
     */
    private fun checkPredecessors() {
        if(predecessor?.isDead() == true){
            predecessor = null
        }
    }

    /**
     *  Initialize finger table of local node
     *  @param id is an arbitrary node already in the network
     */
    private fun isDead(): Boolean {
        Timber.d("Todo")
        return false
    }

    private fun findSuccessor(id: Long): Node? {
        return if (id in (nodeId + 1) until id + 1){
            successor
        } else {
            val n0 = closestPrecedingCluster(id)
            n0?.findSuccessor(id)
        }
    }

    /**
     *  Search finger table for the highest predecessor of id
     *  @param id Id of cl
     */
    private fun closestPrecedingCluster(id: Long): Node? {
        for (i in M-1 downTo 0) {
            if (finger[i]?.nodeId in (nodeId + 1) until id) {
                return successor
            }
        }
        return this
    }

    private fun getChordId(): Long {
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


    override fun toString(): String {
        var text = if(NetworkUtils.hasInternet()) "1" else "0"
        text += ":$nodeId"
        Timber.d("Node toString: %s", text)
        return text
    }
}

