package hudson.plugins.nsiq;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * {@link Builder}의 인스턴스이다.
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqBuilder extends Builder {
	private final String srcDir;
	private final String fileFilter;
	private static final String DELIMITER = ",";

	/**
	 * 기본 생성자
	 * 
	 * @param srcDir
	 *            프로젝트의 설정에서 등록한 소스 디렉토리 정보
	 */
	@DataBoundConstructor
	public NSiqBuilder(String srcDir, String fileFilter) {
		this.srcDir = srcDir;
		this.fileFilter = fileFilter;
	}

	/**
	 * 소스 디렉토리 정보를 리턴한다.
	 */
	public String getSrcDir() {
		return srcDir;
	}

	public String getFileFilter() {
		return fileFilter;
	}

	/**
	 * 소스 디렉토리에 "," 가 있을 경우 여러 개의 배열 값으로 리턴한다.
	 */
	private String[] getSourceDirs() {
		if (srcDir.contains(DELIMITER)) {
			List<String> result = new LinkedList<String>();
			List<String> splitted = Arrays.asList(srcDir.split(DELIMITER));

			for (String dir : splitted) {
				String trim = dir.trim();

				if (StringUtils.isNotEmpty(trim)) {
					result.add(trim);
				}
			}

			return result.toArray(new String[result.size()]);
		} else {
			return new String[] { srcDir.trim() };
		}
	}

	@Override
	public BuildStepDescriptor<Builder> getDescriptor() {
		return DESCRIPTOR;
	}

	/**
	 * 프로젝트 빌드가 완료된 후, {@link Builder}가 실제 수행하는 메소드이다.
	 */
	public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		if (StringUtils.isEmpty(DESCRIPTOR.getNsiqPath())) {
			listener.getLogger().println("[" + Constant.DISPLAY_NAME + "] " + "N'SIQ Collector path is not configured...");
			return true;
		}

		// 실행 파라미터
		ArgumentListBuilder args = new ArgumentListBuilder();

		FilePath moduleRoot = build.getModuleRoots().length == 1 ? build.getModuleRoot() : build.getModuleRoot().getParent();

		// 플러그인 전역 설정에서 지정한 N'SIQ Collector의 실행 경로
		args.add(DESCRIPTOR.getNsiqPath());
		args.add("--csv");
		args.add("-i");
		// -l loc.csv 파일의 저장 위치
		args.add("-l", NSiqUtil.getLocFile(moduleRoot).getRemote());
		// -c complexity.csv 파일의 저장 위치
		args.add("-c", NSiqUtil.getComplexityFile(moduleRoot).getRemote());

		// 파일 필터 직접 설정이 되어 있으면, 파일 필터를 실행시 지정한다.
		if (fileFilter != null && StringUtils.isNotEmpty(fileFilter.trim())) {
			FilePath filterFilePath = new FilePath(moduleRoot, fileFilter.trim());

			if (filterFilePath.exists()) {
				args.add("-f", filterFilePath.getRemote());
			} else {
				listener.getLogger().println("File filter location " + filterFilePath.getRemote() + " is not available. use default filefilter.txt");
			}
		}

		// 소스 디렉토리를 파라미터로 지정
		for (String srcDir : getSourceDirs()) {
			args.add(new FilePath(moduleRoot, srcDir.startsWith(File.separator) ? srcDir.substring(File.separator.length()) : srcDir).getRemote());
		}

		// N'SIQ Collector 실행
		ProcStarter procStater = launcher.launch();
		procStater.cmds(args.toCommandArray());
		procStater.envs(build.getEnvironment(listener));
		procStater.pwd(moduleRoot);
		procStater.stdout(listener.getLogger());
		procStater.stderr(listener.getLogger());
		int result = launcher.launch(procStater).join();
		return (result == 0);
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		// 실행 파일의 위치
		private String nsiqPath;

		public DescriptorImpl() {
			super(NSiqBuilder.class);
			load();
		}

		public String getNsiqPath() {
			return nsiqPath;
		}

		public void setNsiqPath(String nsiqPath) {
			this.nsiqPath = nsiqPath;
		}

		@Override
		public String getDisplayName() {
			return "Execute " + Constant.DISPLAY_NAME;
		}
		
		public FormValidation doCheckPath(StaplerRequest request, StaplerResponse response, @QueryParameter("nsiqPath") String nsiqPath)
				throws IOException, ServletException {
			return FormValidation.validateExecutable(nsiqPath);
		}

		/**
		 * configure 메소드는 플러그인 설정에서 저장 버튼을 클릭했을 때 실행되는 메소드이다.
		 */
		public boolean configure(StaplerRequest req, JSONObject jsonObject) throws FormException {
			nsiqPath = req.getParameter(Constant.URL + ".nsiqPath");
			save();
			return super.configure(req, jsonObject);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return FreeStyleProject.class.equals(jobType);
		}
	}
}
