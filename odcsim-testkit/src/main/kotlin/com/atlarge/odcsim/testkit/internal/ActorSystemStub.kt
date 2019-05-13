/*
 * MIT License
 *
 * Copyright (c) 2019 atlarge-research
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.atlarge.odcsim.testkit.internal

import com.atlarge.odcsim.ActorPath
import com.atlarge.odcsim.ActorRef
import com.atlarge.odcsim.ActorSystem
import com.atlarge.odcsim.Behavior
import com.atlarge.odcsim.Duration
import com.atlarge.odcsim.Instant

/**
 * A stubbed [ActorSystem] for synchronous testing of behavior.
 *
 * @property owner The owner of this actor system.
 */
internal class ActorSystemStub<T : Any>(private val owner: BehaviorTestKitImpl<T>) : ActorSystem<T> {
    override val time: Instant
        get() = owner.time

    override val name: String
        get() = owner.ref.path.name

    override fun run(until: Duration) = throw IllegalStateException("Cannot run ActorSystem within actor")

    override fun send(msg: T, after: Duration) = owner.context.send(owner.context.self, msg, after)

    override fun terminate() {}

    override val path: ActorPath
        get() = owner.ref.path

    override suspend fun <U : Any> spawnSystem(behavior: Behavior<U>, name: String): ActorRef<U> {
        throw IllegalStateException("Cannot spawn system actor")
    }
}