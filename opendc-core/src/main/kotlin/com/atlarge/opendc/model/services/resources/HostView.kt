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

package com.atlarge.opendc.model.services.resources

import com.atlarge.opendc.model.resources.compute.MachineRef
import com.atlarge.opendc.model.resources.compute.MachineStatus
import com.atlarge.opendc.model.resources.compute.host.Host

/**
 * The dynamic information of a [Host] instance that is being tracked by the [ResourceManagementService]. This means
 * that information may not be up-to-date.
 *
 * @property host The static information of the host.
 * @property ref The reference to the host's actor.
 * @property status The status of the machine.
 */
data class HostView(val host: Host, val ref: MachineRef, val status: MachineStatus = MachineStatus.HALT) {
    override fun equals(other: Any?): Boolean = other is HostView && host.uid == other.host.uid
    override fun hashCode(): Int = host.uid.hashCode()
}