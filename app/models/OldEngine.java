package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import analysis.ModelResult;
import query.Scenario;
import utils.RandomString;

public class OldEngine {

	// Currently just starts it but a more advanced version could queue jobs up to fit the available resources
	//------------------------------------------------------------------
	public static String scheduleJob(Scenario scenario) {
		// FIXME: can't create a job directly in a static function? Requires an instance of OldEngine? erf
		return mEngineInstance._internalScheduleJob(scenario);
	}
	//------------------------------------------------------------------
	public static Boolean isJobDone(String jobKey) {

		Job job = mJobs.get(jobKey);
		if (job != null) {
			return job.isDone();
		}
		else return false; // TODO: error
	}
	// NOTE: getting the results can only be done once...then they are gone...
	//------------------------------------------------------------------
	public static List<ModelResult> getJobResults(String jobKey) {
		Job job = mJobs.remove(jobKey);
		if (job != null) {
			List<ModelResult> res = job.getResults();
			return res;
		}
		else return null; // TODO: error
	}
	
	
	private Thread mThread;
	private static Map<String,Job> mJobs = new HashMap<String,Job>();
	private static OldEngine mEngineInstance = new OldEngine();
	
	//------------------------------------------------------------------
	private String _internalScheduleJob(Scenario scenario) {
		String jobId = null;
		
		Integer tryCount = 0;
		while (tryCount++ < 512) {
			jobId = RandomString.get(16);
			if (!mJobs.containsKey(jobId)) {
				break;
			}
		}
		
		Job job = new Job(scenario);
		mJobs.put(jobId, job);
		mThread = new Thread(job, "JobRunner"); mThread.start();
		
		return jobId;
	}
	
	//------------------------------------------------------------------
	public abstract class _Runner implements Runnable {
		_Runner(Scenario scenario) {
			mScenario = scenario;
		}
		Scenario mScenario;
	}
	//------------------------------------------------------------------
	public class Job extends _Runner {
		Thread t1, t2, t3, t4, t5, t6, t7;
		List<ModelResult> mResults = Collections.synchronizedList(new ArrayList<ModelResult>());
		Job(Scenario scenario) {super(scenario);}

		public void provideResults(List<ModelResult> results) {
			synchronized(mResults) {
				mResults.addAll(results);
			}
		}
		public Boolean isDone() {
			synchronized(mResults) {
				// FIXME: the least ideal (hardcoded) part?
				return mResults.size() >= 10;
			}
		}
		public List<ModelResult> getResults() {
			// Not synchronized, because hopefully the job is actually done before calling?
			return mResults;
		}
		public void run() {
			t1 = new Thread(new EthanolNetEnergyIncome_Runner(mScenario, this), "EthanolNetEnergyIncome"); t1.start();
			t2 = new Thread(new P_LossEpic_Runner(mScenario, this), "P_Loss"); t2.start();
			t3 = new Thread(new SoilCarbon_Runner(mScenario, this), "SoilCarbon"); t3.start();
			t4 = new Thread(new PollinatorPestSuppression_Runner(mScenario, this), "PollinatorPest"); t4.start();
			t5 = new Thread(new NitrousOxideEmissions_Runner(mScenario, this), "NitrousOxide"); t5.start();
			t6 = new Thread(new SoilLoss_Runner(mScenario, this), "SoildLoss"); t6.start();
			t7 = new Thread(new HabitatIndex_Runner(mScenario, this), "HabitatIndex"); t7.start();
		}
	}
	
	//------------------------------------------------------------------
	public abstract class _SubRunner extends _Runner {
		Job mJob;
		_SubRunner(Scenario scenario, Job job) { super(scenario); mJob = job; }
	}
	//------------------------------------------------------------------
	public class EthanolNetEnergyIncome_Runner extends _SubRunner {
		EthanolNetEnergyIncome_Runner(Scenario scenario, Job job) {super(scenario, job);}
		public void run() {
			Model_EthanolNetEnergyIncome model = new Model_EthanolNetEnergyIncome();
			mJob.provideResults(model.run(mScenario));
		}
	}
	//------------------------------------------------------------------
	public class P_LossEpic_Runner extends _SubRunner {
		P_LossEpic_Runner(Scenario scenario, Job job) {super(scenario, job);}
		public void run() {
			Model_P_LossEpic model = new Model_P_LossEpic();
			mJob.provideResults(model.run(mScenario));
		}
	}
	//------------------------------------------------------------------
	public class SoilCarbon_Runner extends _SubRunner {
		SoilCarbon_Runner(Scenario scenario, Job job) {super(scenario, job);}
		public void run() {
			Model_SoilCarbon model = new Model_SoilCarbon();
			mJob.provideResults(model.run(mScenario));
		}
	}
	//------------------------------------------------------------------
	public class PollinatorPestSuppression_Runner extends _SubRunner {
		PollinatorPestSuppression_Runner(Scenario scenario, Job job) {super(scenario, job);}
		public void run() {
			Model_PollinatorPestSuppression model = new Model_PollinatorPestSuppression();
			mJob.provideResults(model.run(mScenario));
		}
	}
	//------------------------------------------------------------------
	public class NitrousOxideEmissions_Runner extends _SubRunner {
		NitrousOxideEmissions_Runner(Scenario scenario, Job job) {super(scenario, job);}
		public void run() {
			Model_NitrousOxideEmissions model = new Model_NitrousOxideEmissions();
			mJob.provideResults(model.run(mScenario));
		}
	}
	//------------------------------------------------------------------
	public class SoilLoss_Runner extends _SubRunner {
		SoilLoss_Runner(Scenario scenario, Job job) {super(scenario, job);}
		public void run() {
			Model_Soil_Loss model = new Model_Soil_Loss();
			mJob.provideResults(model.run(mScenario));
		}
	}
	//------------------------------------------------------------------
	public class HabitatIndex_Runner extends _SubRunner {
		HabitatIndex_Runner(Scenario scenario, Job job) {super(scenario, job);}
		public void run() {
			Model_HabitatIndex model = new Model_HabitatIndex();
			mJob.provideResults(model.run(mScenario));
		}
	}
}