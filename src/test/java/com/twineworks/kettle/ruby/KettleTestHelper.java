/*
 * Ruby for pentaho kettle
 * Copyright (C) 2017 Twineworks GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.twineworks.kettle.ruby;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingBuffer;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import static org.assertj.core.api.Assertions.fail;

public class KettleTestHelper {

  public final static String pluginDir;

  static {

    // the plugin directory is created by `mvn package`
    pluginDir = "target/ruby-for-pentaho-kettle";
  }

  public static void initKettle() throws Exception {
    System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS", pluginDir);
    /* speeds up KettleEnvironment.init() */
    System.setProperty("KETTLE_SYSTEM_HOSTNAME", "localhost");
    KettleEnvironment.init();
  }

  // using Thread.stop to kill unresponsive kettle transformations
  @SuppressWarnings("deprecation")
  public static Trans runTransform(String filename, int maxSeconds) throws Exception {

    initKettle();

    TransMeta transMeta = new TransMeta(filename, (Repository) null);
    final Trans transformation = new Trans(transMeta);
    try {
      transformation.setLogLevel(LogLevel.MINIMAL);
      Thread runner = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            transformation.execute(new String[0]);
            transformation.waitUntilFinished();
          } catch (KettleException e) {
            throw new RuntimeException("Kettle exception", e);
          }
        }
      });

      // if the runner dies with an exception, fail the test
      runner.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          System.err.flush();
          e.printStackTrace();
          System.err.flush();
          fail(e.getMessage(), e);
        }
      });

      // start the transformation and wait for it to finish
      runner.start();

      // measure the time while waiting, anything beyond MAX_RUNTIME
      // indicates that the thing hangs
      long startTime = System.currentTimeMillis();
      final long MAX_RUNTIME = maxSeconds * 1000;

      System.err.flush();

      while (runner.isAlive()) {

        runner.join(50);

        if (System.currentTimeMillis() - startTime > MAX_RUNTIME) {
          System.err.println("transformation " + filename + " executing for over " + maxSeconds + " seconds, interrupting");
          runner.interrupt();
          Thread.sleep(3000);
          if (runner.isAlive()) {
            System.err.println("transformation " + filename + " hangs, forcibly stopping");
            runner.stop();
          }
          fail("transformation " + filename + " executing for over " + maxSeconds + " seconds");
        }

      }

      return transformation;
    } finally {
      if (!transformation.isFinished() || transformation.getErrors() > 0) {
        printLog(transformation);
      }
    }

  }

  public static void printLog(Trans trans) {

    LoggingBuffer appender = KettleLogStore.getAppender();
    String logText = appender.getBuffer(trans.getLogChannelId(), true).toString();
    System.err.flush();
    System.err.println("*************************************************************************");
    System.err.println("TRANSFORMATION LOG: \n");
    System.err.println(logText);
    System.err.println("*************************************************************************");
    System.err.flush();
  }

}
