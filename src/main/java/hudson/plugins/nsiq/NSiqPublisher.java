package hudson.plugins.nsiq;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.Result;
import hudson.plugins.nsiq.model.Complexity;
import hudson.plugins.nsiq.model.Loc;
import hudson.plugins.nsiq.model.NSiqResult;
import hudson.plugins.nsiq.model.NSiqSummary;
import hudson.plugins.nsiq.parser.ComplexityParser;
import hudson.plugins.nsiq.parser.LocParser;
import hudson.plugins.nsiq.parser.NSiqResultParser;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * {@link Publisher}의 인스턴스이다.
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqPublisher extends Recorder implements NSiqAware {
	private final double lowRatio;
	private final double highRatio;
	private boolean locView = true;
	private boolean complexityView = true;
	private boolean overView;
	private boolean dailyView;
	private final Encoding encoding;

	/**
	 * 
	 * @param lowRatio
	 *            복잡도가 10인 함수의 비율
	 * @param highRatio
	 *            복잡도가 30인 함수의 비율
	 * @param locView
	 *            LOC 그래프를 보여줄 것인지 설정
	 * @param complexityView
	 *            복잡도 그래프를 보여줄 것인지 설정
	 * @param overView
	 *            복잡도가 10인 함수의 목록을 보여줄 것인지의 여부를 설정
	 * @param dailyView
	 *            그래프를 일 기준으로 그릴 것인지 설정
	 */
	@DataBoundConstructor
	public NSiqPublisher(double lowRatio, double highRatio, boolean locView, boolean complexityView, boolean overView, boolean dailyView,
			Encoding encoding) {
		this.lowRatio = lowRatio;
		this.highRatio = highRatio;
		this.locView = locView;
		this.complexityView = complexityView;
		this.overView = overView;
		this.dailyView = dailyView;
		this.encoding = encoding;
	}

	public boolean isLocView() {
		return locView;
	}

	public boolean isComplexityView() {
		return complexityView;
	}

	public boolean isOverView() {
		return overView;
	}

	public boolean isDailyView() {
		return dailyView;
	}

	@Override
	public BuildStepDescriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	/**
	 * 프로젝트 빌드가 완료된 후, {@link Publisher}가 실제 수행하는 메소드이다.
	 */
	public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		if (build.getResult().equals(Result.FAILURE)) {
			return false;
		}
		FilePath moduleRoot = build.getModuleRoots().length == 1 ? build.getModuleRoot() : build.getModuleRoot().getParent();

		FilePath locFile = NSiqUtil.getLocFile(moduleRoot);
		FilePath complexityFile = NSiqUtil.getComplexityFile(moduleRoot);

		if (!locFile.exists() || !complexityFile.exists()) {
			return false;
		}

		// loc 정보 parsing
		LocParser locParser = new LocParser(locFile);
		// locParser.setWriter(listener.getLogger());
		List<Loc> locList = locParser.parse();

		// complexity 정보 parsing
		ComplexityParser complexityParser = new ComplexityParser(complexityFile);
		// complexityParser.setWriter(listener.getLogger());
		List<Complexity> complexityList = complexityParser.parse();

		NSiqResultParser nsiqCollectorParser = new NSiqResultParser(locList, complexityList);
		List<NSiqResult> nsiqResult = nsiqCollectorParser.parse();
		NSiqSummary summary = nsiqCollectorParser.getSummary();

		NSiqTarget target = NSiqTarget.load(build, nsiqResult);
		target.setOwner(null);
		NSiqUtil.getDataFile(build).write(target);
		target.setOwner(build);
		locFile.delete();
		complexityFile.delete();

		// N'SIQ Collector의 결과 정보를 BuildAction에 추가
		final NSiqBuildAction action = NSiqBuildAction.load(build, summary, target, lowRatio, highRatio);
		build.getActions().add(action);

		return true;
	}

	/**
	 * 플러그인에 대한 ProjectAction 인스턴스를 리턴한다.
	 */
	@SuppressWarnings("unchecked")
	public Action getProjectAction(Project project) {
		return new NSiqProjectAction(project);
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		@Override
		public String getDisplayName() {
			return "Publish " + Constant.DISPLAY_NAME;
		}

		public String getLowRatio(NSiqPublisher instance) {
			if (instance == null) {
				return "10.0";
			}

			return NSiqUtil.getFormattedComplexity(instance.lowRatio);
		}

		public String getHighRatio(NSiqPublisher instance) {
			if (instance == null) {
				return "0.5";
			}

			return NSiqUtil.getFormattedComplexity(instance.highRatio);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return FreeStyleProject.class.equals(jobType);
		}
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	public Encoding getEncoding() {
		return (encoding == null) ? Encoding.UTF_8 : encoding;
	}

}
