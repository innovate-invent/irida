package ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import ca.corefacility.bioinformatics.irida.exceptions.galaxy.GalaxyUserNoRoleException;
import ca.corefacility.bioinformatics.irida.exceptions.galaxy.GalaxyUserNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.galaxy.NoGalaxyContentFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.galaxy.NoLibraryFoundException;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyAccountEmail;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyFolderPath;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyProjectName;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.LibrariesClient;
import com.github.jmchilton.blend4j.galaxy.RolesClient;
import com.github.jmchilton.blend4j.galaxy.UsersClient;
import com.github.jmchilton.blend4j.galaxy.beans.Library;
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent;
import com.github.jmchilton.blend4j.galaxy.beans.Role;
import com.github.jmchilton.blend4j.galaxy.beans.User;

/**
 * Class containing methods used to search for information within a Galaxy
 * instance.
 * 
 * @author aaron
 * 
 */
public class GalaxySearch {
	private GalaxyInstance galaxyInstance;

	/**
	 * Builds a new Galaxy search object around the passed GalaxyInstance.
	 * 
	 * @param galaxyInstance
	 *            The GalaxyInstance object used to connect to Galaxy.
	 */
	public GalaxySearch(GalaxyInstance galaxyInstance) {
		checkNotNull(galaxyInstance, "galaxyInstance is null");

		this.galaxyInstance = galaxyInstance;
	}

	/**
	 * Given an email, finds a corresponding users private Role object in Galaxy
	 * with that email.
	 * 
	 * @param email
	 *            The email of the user to search.
	 * @return A private Role object of the user with the corresponding email.
	 * @throws GalaxyUserNoRoleException
	 *             If no role for the user could be found.
	 */
	public Role findUserRoleWithEmail(GalaxyAccountEmail email) throws GalaxyUserNoRoleException {
		checkNotNull(email, "email is null");

		RolesClient rolesClient = galaxyInstance.getRolesClient();
		if (rolesClient != null) {
			Optional<Role> r = rolesClient.getRoles().stream().filter((role) -> role.getName().equals(email.getName()))
					.findFirst();
			if (r.isPresent()) {
				return r.get();
			}
		}

		throw new GalaxyUserNoRoleException("No role found for " + email + " in Galaxy "
				+ galaxyInstance.getGalaxyUrl());
	}

	/**
	 * Given an email, finds a corresponding User object in Galaxy with that
	 * email.
	 * 
	 * @param email
	 *            The email of the user to search.
	 * @return A User object of the user with the corresponding email.
	 * @throws GalaxyUserNotFoundException
	 *             If the user could not be found.
	 */
	public User findUserWithEmail(GalaxyAccountEmail email) throws GalaxyUserNotFoundException {
		checkNotNull(email, "email is null");

		UsersClient usersClient = galaxyInstance.getUsersClient();
		if (usersClient != null) {
			Optional<User> u = usersClient.getUsers().stream()
					.filter((user) -> user.getEmail().equals(email.getName())).findFirst();
			if (u.isPresent()) {
				return u.get();
			}
		}

		throw new GalaxyUserNotFoundException(email, getGalaxyUrl());
	}

	/**
	 * Gets the URL of the Galaxy instance we are connected to.
	 * 
	 * @return A String of the URL of the Galaxy instance we are connected to.
	 */
	private URL getGalaxyUrl() {
		try {
			return new URL(galaxyInstance.getGalaxyUrl());
		} catch (MalformedURLException e) {
			// This should never really occur, don't force all calling methods
			// to catch exception
			throw new RuntimeException("Galaxy URL is malformed", e);
		}
	}

	/**
	 * Given a library ID, searches for the corresponding Library object.
	 * 
	 * @param libraryId
	 *            The libraryId to search for.
	 * @return A Library object for this Galaxy library.
	 * @throws NoLibraryFoundException
	 *             If a library could not be found.
	 */
	public Library findLibraryWithId(String libraryId) throws NoLibraryFoundException {
		checkNotNull(libraryId, "libraryId is null");

		LibrariesClient librariesClient = galaxyInstance.getLibrariesClient();
		List<Library> libraries = librariesClient.getLibraries();
		Optional<Library> library = libraries.stream().filter((lib) -> lib.getId().equals(libraryId)).findFirst();
		if (library.isPresent()) {
			return library.get();
		}

		throw new NoLibraryFoundException("No library found for id " + libraryId + " in Galaxy "
				+ galaxyInstance.getGalaxyUrl());
	}

	/**
	 * Gets a Map listing all contents of the passed Galaxy library to the
	 * LibraryContent object.
	 * 
	 * @param libraryId
	 *            The library to get all contents from.
	 * @return A Map mapping the path of the library content to the
	 *         LibraryContent object.
	 * @throws NoGalaxyContentFoundException
	 *             If no library could be found.
	 */
	public Map<String, LibraryContent> libraryContentAsMap(String libraryId) throws NoGalaxyContentFoundException {
		checkNotNull(libraryId, "libraryId is null");

		LibrariesClient librariesClient = galaxyInstance.getLibrariesClient();
		List<LibraryContent> libraryContents = librariesClient.getLibraryContents(libraryId);

		if (libraryContents != null) {
			Map<String, LibraryContent> map = libraryContents.stream().collect(
					Collectors.toMap(LibraryContent::getName, Function.identity()));
			return map;
		}

		throw new NoGalaxyContentFoundException("Could not find library content for id " + libraryId + " in Galaxy "
				+ galaxyInstance.getGalaxyUrl());
	}

	/**
	 * Given a library name, searches for a list of matching Library objects.
	 * 
	 * @param libraryName
	 *            The name of the library to search for.
	 * @return A list of Library objects matching the given name.
	 * @throws NoLibraryFoundException
	 *             If no libraries could be found.
	 */
	public List<Library> findLibraryWithName(GalaxyProjectName libraryName) throws NoLibraryFoundException {
		checkNotNull(libraryName, "libraryName is null");

		LibrariesClient librariesClient = galaxyInstance.getLibrariesClient();
		List<Library> allLibraries = librariesClient.getLibraries();

		if (allLibraries != null) {
			List<Library> libraries = allLibraries.stream()
					.filter((lib) -> lib.getName().equals(libraryName.getName())).collect(Collectors.toList());

			if (libraries.size() > 0) {
				return libraries;
			}
		}

		throw new NoLibraryFoundException("No library could be found with name " + libraryName + " in Galaxy "
				+ galaxyInstance.getGalaxyUrl());
	}

	/**
	 * Given a libraryId and a folder name, search for the corresponding
	 * LibraryContent object within this library.
	 * 
	 * @param libraryId
	 *            The ID of the library to search for.
	 * @param folderName
	 *            The name of the folder to search for (only finds first
	 *            instance of this folder name).
	 * @return A LibraryContent within the given library with the given name, or
	 *         null if no such folder exists.
	 * @throws NoGalaxyContentFoundException
	 *             If no Galaxy content was found.
	 */
	public LibraryContent findLibraryContentWithId(String libraryId, GalaxyFolderPath folderPath)
			throws NoGalaxyContentFoundException {
		checkNotNull(libraryId, "libraryId is null");
		checkNotNull(folderPath, "folderPath is null");

		LibrariesClient librariesClient = galaxyInstance.getLibrariesClient();
		List<LibraryContent> libraryContents = librariesClient.getLibraryContents(libraryId);

		if (libraryContents != null) {
			Optional<LibraryContent> content = libraryContents.stream()
					.filter(c -> c.getType().equals("folder") && c.getName().equals(folderPath.getName())).findFirst();
			if (content.isPresent()) {
				return content.get();
			}
		}

		throw new NoGalaxyContentFoundException("Could not find library content for id " + libraryId + " in Galaxy "
				+ galaxyInstance.getGalaxyUrl());
	}

	/**
	 * Determines if the passed Galaxy user exists within the Galaxy instance.
	 * 
	 * @param galaxyUserEmail
	 *            The user email address to check.
	 * @return True if this user exists, false otherwise.
	 */
	public boolean galaxyUserExists(GalaxyAccountEmail galaxyUserEmail) {
		try {
			return findUserWithEmail(galaxyUserEmail) != null;
		} catch (GalaxyUserNotFoundException e) {
			return false;
		}
	}

	/**
	 * Determines if a library with the given name exists.
	 * 
	 * @param libraryName
	 *            The name of the library to check.
	 * @return True if a library with this name exists, false otherwise.
	 */
	public boolean libraryExists(GalaxyProjectName libraryName) {
		try {
			List<Library> libraries = findLibraryWithName(libraryName);
			return libraries != null && libraries.size() > 0;
		} catch (NoLibraryFoundException e) {
			return false;
		}
	}

	/**
	 * Determine if the given folderPath exists within a library with the given
	 * id.
	 * 
	 * @param libraryId
	 *            The id of the library to check.
	 * @param folderPath
	 *            A path within this library to check.
	 * @return True if this path exists within this library, false otherwise.
	 */
	public boolean libraryContentExists(String libraryId, GalaxyFolderPath folderPath) {
		try {
			LibraryContent content = findLibraryContentWithId(libraryId, folderPath);
			return content != null;
		} catch (NoGalaxyContentFoundException e) {
			return false;
		}
	}

	/**
	 * Determines if a role exists for the given user.
	 * 
	 * @param galaxyUserEmail
	 *            The user to search for a role.
	 * @return True if a role exists for the user, false otherwise.
	 */
	public boolean userRoleExistsFor(GalaxyAccountEmail galaxyUserEmail) {
		try {
			Role role = findUserRoleWithEmail(galaxyUserEmail);
			return role != null;
		} catch (GalaxyUserNoRoleException e) {
			return false;
		}
	}
}