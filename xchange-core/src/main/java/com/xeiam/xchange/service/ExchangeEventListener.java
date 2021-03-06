/**
 * Copyright (C) 2012 Xeiam LLC http://xeiam.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
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
package com.xeiam.xchange.service;

import java.util.concurrent.BlockingQueue;

/**
 * <p>
 * Listener to provide the following to API:
 * </p>
 * <ul>
 * <li>Callback methods for exchange events</li>
 * </ul>
 * <p>
 * A {@link ExchangeEventListener} is normally executed in a client thread using an executor service and to facilitate this the {@link RunnableExchangeEventListener} is provided as an extension point
 * </p>
 */
public interface ExchangeEventListener {

  /**
   * @param marketDataEvents The blocking queue that links the XChange thread pool to the client thread pool
   */
  void setExchangeEventQueue(BlockingQueue<ExchangeEvent> marketDataEvents);

}
