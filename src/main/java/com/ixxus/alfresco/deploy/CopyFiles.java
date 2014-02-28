package com.ixxus.alfresco.deploy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.alfresco.deployment.FSDeploymentRunnable;
import org.alfresco.deployment.FileType;
import org.alfresco.deployment.impl.server.DeployedFile;
import org.alfresco.deployment.impl.server.Deployment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Harry Moore - Ixxus
 * Alfresco FSR post-deploy process. Copy deployed files to another location.
 * Target location is specified in the configuration of this "bean"
 */
public class CopyFiles implements FSDeploymentRunnable {
    private static final long serialVersionUID = 1L;
    private static final String CONFIG_FILE = "deployment.properties";
    private static final Log logger = LogFactory.getLog(CopyFiles.class);
    private List<String> files = new ArrayList<String>();

    /**
     * Absolute path where you want files copied)
     * to go.
     * @param String targetPath
     */
    private String targetPath = null;

    /**
     * true|false. Should copy be simulated and logged only (true) or should file copy be performed (false)
     *
     * @param Boolean dryRun
     */
    private Boolean dryRun = false;

    /**
     * The path where files are deployed to for this target. rootDirectory
     * property of the target.
     */
    private String deploymentBasePath = null;

    @Override
    public void run() {
        logger.info(String.format("run() dryRun=%s", dryRun));

        File targetDir = new File(targetPath);
        if(!targetDir.exists() || !targetDir.isDirectory()) {
            logger.error(String.format("Could not copy files to %s. It either does not exist or it is not a folder", targetDir));
            return;
        }

        for(String filePath : files ) {
            final File sourceFile = new File(deploymentBasePath + filePath);
            final File targetFile = new File(targetPath + filePath);

            logger.debug(String.format("Copying %s to %s", sourceFile, targetFile));
            if(!dryRun) {
                try {
                    FileUtils.copyFile(sourceFile, targetFile);
                } catch (IOException ioe) {
                    logger.warn(String.format("Error copying %s to %s", sourceFile, targetFile, ioe));
                }
            }
        }

    }

    @Override
    public void init(Deployment deployment) {
        logger.debug(String.format("init() deployment state=%s,  deploymentBasePath=%s, targetPath=%s, dryRun=%s", deployment.getState(), deploymentBasePath, targetPath, dryRun));
        for (DeployedFile file : deployment) {
            logger.info(String.format("file type=%s, path=%s, preLocation=%s", file.getType(), file.getPath(), file.getPreLocation()));
            if (file.getType() == FileType.FILE) {
                files.add(file.getPath());
            }
        }
    }

    private void assertFileExists(File file) {
        if (!file.exists()) {
            throw new IllegalStateException("No file at " + file.getAbsolutePath());
        }
    }

    public Boolean getDryRun() { return dryRun; }

    public void setDryRun(Boolean dryRun) { this.dryRun = dryRun; }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getDeploymentBasePath() {
        return deploymentBasePath;
    }

    public void setDeploymentBasePath(String deploymentBasePath) {
        this.deploymentBasePath = deploymentBasePath;
    }

}
