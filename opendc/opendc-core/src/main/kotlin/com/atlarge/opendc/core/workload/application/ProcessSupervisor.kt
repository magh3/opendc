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

package com.atlarge.opendc.core.workload.application

import com.atlarge.odcsim.Behavior
import com.atlarge.odcsim.ProcessContext
import com.atlarge.odcsim.ReceiveRef
import com.atlarge.opendc.core.resources.compute.ProcessingElement
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive

/**
 * An interface for supervising processes.
 */
interface ProcessSupervisor {
    /**
     * This method is invoked when the setup of an application completed successfully.
     *
     * @param pid The process id of the process that has been initialized.
     */
    fun onReady(pid: Pid) {}

    /**
     * This method is invoked when a process informs the machine that it is running with the
     * estimated resource utilization until a specified point in time.
     *
     * @param pid The process id of the process that is running.
     * @param utilization The utilization of the cpu cores as a percentage.
     * @param until The point in time until which the utilization is valid.
     */
    fun onConsume(pid: Pid, utilization: Map<ProcessingElement, Double>, until: Long) {}

    /**
     * This method is invoked when a process exits.
     *
     * @property pid A reference to the application instance.
     * @property status The exit code of the task, where zero means successful.
     */
    fun onExit(pid: Pid, status: Int) {}

    companion object {
        /**
         * Create the [Behavior] for a [ProcessSupervisor].
         *
         * @param supervisor The supervisor to create the behavior for.
         */
        suspend operator fun invoke(ctx: ProcessContext, supervisor: ProcessSupervisor, main: ReceiveRef<ProcessEvent>) {
            val inlet = ctx.listen(main)
            coroutineScope {
                while (isActive) {
                    when (val msg = inlet.receive()) {
                        is ProcessEvent.Ready -> supervisor.onReady(msg.pid)
                        is ProcessEvent.Consume -> supervisor.onConsume(msg.pid, msg.utilization, msg.until)
                        is ProcessEvent.Exit -> supervisor.onExit(msg.pid, msg.status)
                    }
                }
            }
            inlet.close()
        }
    }
}
