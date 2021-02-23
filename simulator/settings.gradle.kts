/*
 * Copyright (c) 2017 AtLarge Research
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
rootProject.name = "opendc-simulator"

include(":opendc-platform")
include(":opendc-core")
include(":opendc-compute:opendc-compute-core")
include(":opendc-compute:opendc-compute-simulator")
include(":opendc-workflows")
include(":opendc-format")
include(":opendc-experiments:opendc-experiments-sc18")
include(":opendc-experiments:opendc-experiments-capelin")
include(":opendc-runner-web")
include(":opendc-simulator:opendc-simulator-core")
include(":opendc-simulator:opendc-simulator-compute")
include(":opendc-simulator:opendc-simulator-failures")
include(":opendc-trace:opendc-trace-core")
include(":opendc-harness")
include(":opendc-utils")
