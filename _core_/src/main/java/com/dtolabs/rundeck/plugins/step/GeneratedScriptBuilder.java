/*
 * Copyright 2016 SimplifyOps, Inc. (http://simplifyops.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* GeneratedScriptBuilder.java
* 
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: 11/20/12 11:47 AM
* 
*/
package com.dtolabs.rundeck.plugins.step;


/**
 * A simple implementation of {@link GeneratedScript}, which can be created via static factory methods {@link
 * #script(String, String[])} or {@link #command(String...)}
 *
 * @author Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
 */
public class GeneratedScriptBuilder implements FileExtensionGeneratedScript {
    private String script;
    private String[] args;
    private String[] command;
    private String fileExtension;


    private GeneratedScriptBuilder(final String script, final String[] args) {
        this.script = script;
        this.args = args;
    }

    private GeneratedScriptBuilder(final String script, final String[] args, final String fileExtension) {
        this.script = script;
        this.args = args;
        this.fileExtension = fileExtension;
    }

    private GeneratedScriptBuilder(final String[] command) {
        this.command = command;
    }

    public String getScript() {
        return script;
    }

    public String[] getArgs() {
        return args;
    }

    public String[] getCommand() {
        return command;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Create a script
     *
     * @param script the script text
     * @param args   the arguments for the script
     *
     * @return the generated script
     */
    public static GeneratedScript script(final String script, final String[] args) {
        return new GeneratedScriptBuilder(script, args);
    }

    /**
     * Create a script
     *
     * @param script        the script text
     * @param args          the arguments for the script
     * @param fileExtension file extension
     *
     * @return the generated script
     */
    public static GeneratedScript script(final String script, final String[] args, final String fileExtension) {
        return new GeneratedScriptBuilder(script, args, fileExtension);
    }

    /**
     * Create a command
     *
     * @param command the command and arguments
     *
     * @return the generated script
     */
    public static GeneratedScript command(final String... command) {
        return new GeneratedScriptBuilder(command);
    }

}
