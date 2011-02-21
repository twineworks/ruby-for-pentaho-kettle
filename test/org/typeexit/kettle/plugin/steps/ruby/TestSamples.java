package org.typeexit.kettle.plugin.steps.ruby;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

//
// NOTE: this is a blackbox test that works on distribution files
// It should only be run run after "ant testdist" has put a test distribution into place
//
public class TestSamples extends TestCase {
	
	public void testRunAllSamples() throws KettleException, InterruptedException{
		
		String projectDir = System.getProperty("user.dir");
		System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS", projectDir+"/testdist"); // the plugin directory is in "dist"
		
		KettleEnvironment.init();
		
		String jobfile = projectDir+"/test/org/typeexit/kettle/plugin/steps/ruby/files/run_all_samples.kjb";
		
		JobMeta jobMeta = new JobMeta(jobfile, null, null);
		final Job job = new Job(null, jobMeta);
		
        job.initializeVariablesFrom(null);
        job.setLogLevel(LogLevel.MINIMAL);
        job.getJobMeta().setInternalKettleVariables(job);
        job.copyParametersFrom(job.getJobMeta());
		job.activateParameters();

		Thread jobRunner = new Thread(new Runnable(){

			@Override
			public void run() {
				job.start();
				job.waitUntilFinished();
				 
			}
		});;
		
		// start the job and wait for it to finish
		jobRunner.start();
		
		// measure the time while waiting, anything beyond MAX_RUNTIME
		// indicates that the thing hangs
		long startTime = System.currentTimeMillis(); 
		final int MAX_RUNTIME = 120 * 1000;
		
		while(jobRunner.isAlive()){
			
			Thread.sleep(1000);
			
			if (System.currentTimeMillis() - startTime > MAX_RUNTIME){
				fail("samples job seems to be hanging for "+(MAX_RUNTIME/1000)+" seconds");
			}
		}

		// make sure there's no errors when the job finishes 
		Result result = job.getResult();
		assertEquals(0, result.getNrErrors());
		
	}

}
