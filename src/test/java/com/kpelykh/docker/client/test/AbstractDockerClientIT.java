package com.kpelykh.docker.client.test;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDockerClientIT extends Assert {

	public static final Logger LOG = LoggerFactory
			.getLogger(AbstractDockerClientIT.class);

	protected DockerClient dockerClient;

	protected List<String> tmpImgs;
	protected List<String> tmpContainers;


	public void beforeTest() throws DockerException {
		LOG.info("======================= BEFORETEST =======================");

        dockerClient = createDockerClient();

        LOG.info("Docker version " + dockerClient.version().getVersion());

        LOG.info("Creating image 'busybox'");
		// need to block until image is pulled completely
		logResponseStream(dockerClient.pull("busybox"));

		assertNotNull(dockerClient);
		LOG.info("======================= END OF BEFORETEST =======================\n\n");
	}

    private DockerClient createDockerClient() throws DockerException {
        if (System.getProperty("docker.url") != null) {
            try {
                LOG.info("docker.url " + System.getProperty("docker.url"));
                return getDockerClient(URI.create(System.getProperty("docker.url")));
            } catch (Exception e) {
                throw new RuntimeException("Unable to use -Ddocker.url '" + System.getProperty("docker.url") + "'", e);
            }
        }

        if (System.getenv("DOCKER_HOST") != null) {
            try {
                LOG.info("$DOCKER_HOST " + System.getenv("DOCKER_HOST"));
                return getDockerClient(URI.create(System.getenv("DOCKER_HOST")));
            } catch (Exception e) {
                throw new RuntimeException("Unable to use $DOCKER_HOST '" + System.getenv("DOCKER_HOST") + "'", e);
            }
        }

        try {
            return getDockerClient(URI.create("http://localhost:4243"));
        } catch (ClientHandlerException ignore) {
        }

        try {
            return getDockerClient(URI.create("http://localhost:2375"));
        } catch (ClientHandlerException ignore) {
        }

        throw new RuntimeException("Unable to detect Docker; please use $DOCKER_HOST, -Ddocker.url, or make sure the Docker deamon is running on either port 4243 or 2375");
    }

    private DockerClient getDockerClient(URI host) throws DockerException {
        final String url = String.format("http://%s:%d", host.getHost(), host.getPort());

        LOG.info("Connecting to Docker server at " + url);
        DockerClient dockerClient = new DockerClient(url);
        dockerClient.version();
        return dockerClient;
    }

    public void afterTest() {
		LOG.info("======================= END OF AFTERTEST =======================");
	}


	public void beforeMethod(Method method) {
	        tmpContainers = new ArrayList<String>();
	        tmpImgs = new ArrayList<String>();
		LOG.info(String
				.format("################################## STARTING %s ##################################",
						method.getName()));
	}

	public void afterMethod(ITestResult result) {

		for (String container : tmpContainers) {
			LOG.info("Cleaning up temporary container {}", container);
			try {
				dockerClient.stopContainer(container);
				dockerClient.kill(container);
				dockerClient.removeContainer(container);
			} catch (DockerException ignore) {
			}
		}

		for (String image : tmpImgs) {
			LOG.info("Cleaning up temporary image {}", image);
			try {
				dockerClient.removeImage(image);
			} catch (DockerException ignore) {
			}
		}

		LOG.info(
				"################################## END OF {} ##################################\n",
				result.getName());
	}

	protected String logResponseStream(ClientResponse response)  {
		String responseString;
		try {
			responseString = DockerClient.asString(response);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		LOG.info("Container log: {}", responseString);
		return responseString;
	}

}
