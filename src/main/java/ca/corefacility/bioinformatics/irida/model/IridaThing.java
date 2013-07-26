/*
 * Copyright 2013 Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.corefacility.bioinformatics.irida.model;

import javax.validation.constraints.NotNull;

/**
 * An interface for all model classes in the IRIDA system
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
public interface IridaThing extends Timestamped{
    @NotNull
    public String getLabel();
     
    public Long getId();
    
    public boolean isEnabled();
    public void setEnabled(boolean valid);
    
}
