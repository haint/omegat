/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2014 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.team2;

import gen.core.project.RepositoryDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Interface for any remote repository implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IRemoteRepository2 {
    /**
     * Initialize repository provider.
     * 
     * @param repo
     *            repository description instance
     * @param dir
     *            directory for store files
     */
    void init(RepositoryDefinition repo, File dir) throws Exception;

    /**
     * Get file version.
     */
    String getFileVersion(String file) throws Exception;

    void switchToVersion(String version) throws Exception;

    void addForCommit(String path) throws Exception;

    /**
     * Commit to repository after specified version, or after any version if 'null' specified.
     * 
     * @param path
     *            path for commit
     * @param version
     *            if version defined, then commit must be just after this version. Otherwise(if remote
     *            repository was updated after rebase), commit shouldn't be processed and should return null.
     *            If version is null, then commit can be after any version, i.e. previous version shouldn't be
     *            checked.
     * @param comment
     *            comment for commit
     * @return new version if commit was processed, or null if remote repository was updated by other user
     */
    String commit(String version, String comment) throws Exception;

    /**
     * Given repository does not exist on the remote machine
     *
     */
    @SuppressWarnings("serial")
    public static class BadRepositoryException extends Exception {
        public BadRepositoryException(String message) {
            super(message);
        }
    }

    /**
     * Network problems. E.g. no internet available.
     */
    @SuppressWarnings("serial")
    public static class NetworkException extends Exception {
        public NetworkException(Throwable ex) {
            super(ex);
        }
    }
    public static class Credentials {
        public String username = null;
        public char[] password = null;
        public boolean saveAsPlainText = false;
        public boolean readOnly = false;
        public String fingerprint = null;
        
        public void clear() {
            username = null;
            if (password != null) {
                Arrays.fill(password, '0');
            }
            password = null;
            fingerprint = null;
        }
        
        /**
         * key in properties file that contains credentials
         */
        private static final String PKEY_USERNAME = "username";
        private static final String PKEY_PASSWORD = "password";
        private static final String PKEY_FINGERPRINT = "RSAkeyfingerprint";
        
        public static Credentials fromFile(File file) throws FileNotFoundException, IOException {
            Credentials result = new Credentials();
            if (!file.canRead()) {
                throw new IOException("Insufficient permissions to read file: " + file);
            }
            Properties p = new Properties();
            p.load(new FileInputStream(file));
            result.username = p.getProperty(PKEY_USERNAME);
            result.password = p.getProperty(PKEY_PASSWORD).toCharArray();
            result.fingerprint = p.getProperty(PKEY_FINGERPRINT);
            result.saveAsPlainText = true;
            return result;
        }
        
        /**
         * Saves username, password and fingerprint (if known) to plain text file.
         */
        public void saveToPlainTextFile(File file) throws FileNotFoundException, IOException {
            Properties p = new Properties();
            p.setProperty(PKEY_USERNAME, username);
            p.setProperty(PKEY_PASSWORD, String.valueOf(password));
            if (fingerprint != null) {
                p.setProperty(PKEY_FINGERPRINT, fingerprint);
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            p.store(new FileOutputStream(file), "Remote access credentials for OmegaT project");
        }
        
        public Credentials clone() {
            Credentials clone = new Credentials();
            clone.username = username;
            if (password != null) {
                clone.password = Arrays.copyOf(password, password.length);
            }
            clone.fingerprint = fingerprint;
            clone.saveAsPlainText = saveAsPlainText;
            clone.readOnly = readOnly;
            return clone;
        }
    }
}