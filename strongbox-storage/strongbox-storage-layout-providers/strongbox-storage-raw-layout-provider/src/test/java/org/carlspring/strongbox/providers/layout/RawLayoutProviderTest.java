package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.config.RawLayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.testing.TestCaseWithRepository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = RawLayoutProviderTestConfig.class)
public class RawLayoutProviderTest
        extends TestCaseWithRepository
{

    public static final String STORAGE = "storage-raw";

    public static final String REPOSITORY = "raw-releases";

    @Inject
    ConfigurationManagementService configurationManagementService;

    @Inject
    StorageManagementService storageManagementService;

    @Inject
    RepositoryManagementService repositoryManagementService;

    @Inject
    ArtifactManagementService artifactManagementService;
    
    @Inject
    ArtifactResolutionService artifactResolutionService;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE, REPOSITORY, RawLayoutProvider.ALIAS));

        return repositories;
    }


    @Before
    public void setUp()
            throws Exception
    {
        Configuration configuration = configurationManagementService.getConfiguration();

        if (configuration.getStorage(STORAGE) == null)
        {
            createStorage(STORAGE);
        }

        if (configuration.getStorage(STORAGE).getRepository(REPOSITORY) == null)
        {
            createRepository(STORAGE, REPOSITORY);
        }
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void testNullArtifactCoordinates()
    {
        NullArtifactCoordinates coordinates = new NullArtifactCoordinates("foo/bar/blah.bz2");

        System.out.println("coordinates.toPath(): " + coordinates.toPath());
    }

    @Test
    public void testDeployAndResolveArtifact()
            throws Exception
    {
        String path = "foo/bar.zip";

        // Deploy the artifact
        artifactManagementService.validateAndStore(STORAGE,
                                                   REPOSITORY,
                                                   path,
                                                   createZipFile());

        
        File artifactFile = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/" + STORAGE + "/" + REPOSITORY + "/" + path);
        assertTrue("Failed to deploy artifact!", artifactFile.exists());
        assertTrue("Failed to deploy artifact!", artifactFile.length() > 0);

        // Attempt to re-deploy the artifact
        try
        {
            artifactManagementService.validateAndStore(STORAGE,
                                                       REPOSITORY,
                                                       path,
                                                       createZipFile());
        }
        catch (Exception e)
        {
            if (e.getMessage().contains("repository does not allow artifact re-deployment"))
            {
                // This is expected
                System.out.println("Successfully declined to re-deploy " + artifactFile.getPath() + "!");
            }
            else
            {
                throw e;
            }
        }

        // Attempt to resolve the artifact
        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(STORAGE, REPOSITORY, path);
        try (InputStream is = artifactResolutionService.getInputStream(repositoryPath))
        {
            int total = 0;
            int len;
            final int size = 4096;
            byte[] bytes = new byte[size];

            while ((len = is.read(bytes, 0, size)) != -1)
            {
                total += len;
            }

            assertTrue("Failed to resolve artifact!", total > 0);
        }
    }

    private void createRepository(String storageId, String repositoryId)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setAllowsRedeployment(true);
        repository.setLayout(RawLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(Collections.emptySet());

        configurationManagementService.saveRepository(storageId, repository);

        // Create the repository
        repositoryManagementService.createRepository(storageId, repositoryId);
    }

    private void createStorage(String storageId)
            throws IOException, JAXBException
    {
        MutableStorage storage = new MutableStorage(storageId);

        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }

    private InputStream createZipFile()
            throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(os))
        {
            ZipEntry entry = new ZipEntry("dummy-file.txt");

            zos.putNextEntry(entry);
            zos.write("this is a test file".getBytes());
            zos.closeEntry();
        }

        return new ByteArrayInputStream(os.toByteArray());
    }

}
