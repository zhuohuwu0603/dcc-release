/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.release.job.document.util;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.of;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.Map;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

import org.icgc.dcc.release.core.document.BaseDocumentType;
import org.icgc.dcc.release.job.document.model.BroadcastType;
import org.icgc.dcc.release.job.document.model.CollectionFields;
import org.icgc.dcc.release.job.document.model.DocumentFields;
import org.icgc.dcc.release.job.document.model.DocumentTypeAttributes;
import org.icgc.dcc.release.job.document.task.DiagramIndexTask;
import org.icgc.dcc.release.job.document.task.DonorCentricIndexTask;
import org.icgc.dcc.release.job.document.task.DonorIndexTask;
import org.icgc.dcc.release.job.document.task.DonorTextIndexTask;
import org.icgc.dcc.release.job.document.task.GeneCentricIndexTask;
import org.icgc.dcc.release.job.document.task.GeneIndexTask;
import org.icgc.dcc.release.job.document.task.GeneSetIndexTask;
import org.icgc.dcc.release.job.document.task.GeneSetTextIndexTask;
import org.icgc.dcc.release.job.document.task.GeneTextIndexTask;
import org.icgc.dcc.release.job.document.task.MutationCentricIndexTask;
import org.icgc.dcc.release.job.document.task.MutationTextIndexTask;
import org.icgc.dcc.release.job.document.task.ObservationCentricIndexTask;
import org.icgc.dcc.release.job.document.task.ProjectIndexTask;
import org.icgc.dcc.release.job.document.task.ProjectTextIndexTask;
import org.icgc.dcc.release.job.document.task.ReleaseIndexTask;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@NoArgsConstructor(access = PRIVATE)
public final class DocumentTypes {

  private static final Map<BaseDocumentType, DocumentTypeAttributes> DOCUMENT_TYPES = defineDocumentTypes();

  public static String getIndexClassName(@NonNull BaseDocumentType documentType) {
    return DOCUMENT_TYPES.get(documentType).indexClassName();
  }

  public static Collection<BroadcastType> getBroadcastDependencies(@NonNull BaseDocumentType documentType) {
    return DOCUMENT_TYPES.get(documentType).broadcastDependencies();
  }

  public static DocumentFields getFields(@NonNull BaseDocumentType documentType) {
    return DOCUMENT_TYPES.get(documentType).fields();
  }

  private static Map<BaseDocumentType, DocumentTypeAttributes> defineDocumentTypes() {
    val documentTypes =
        ImmutableMap
            .<BaseDocumentType, DocumentTypeAttributes> builder()
            .put(BaseDocumentType.DIAGRAM_TYPE, attributes().indexClassName(DiagramIndexTask.class.getName()))
            .put(BaseDocumentType.RELEASE_TYPE, attributes().indexClassName(ReleaseIndexTask.class.getName()))

            .put(BaseDocumentType.GENE_SET_TYPE,
                attributes()
                    .indexClassName(GeneSetIndexTask.class.getName())
                    .fields(fields()
                        .geneFields(
                            geneFields()
                                .includedFields(
                                    "_gene_id",

                                    // Gene sets
                                    "sets.id",
                                    "sets.type",

                                    "project._project_id",
                                    "donor")
                        )
                        .projectFields(
                            projectFields()
                                .includedFields(
                                    "_project_id",
                                    "project_name",
                                    "primary_site",
                                    "tumour_type",
                                    "tumour_subtype",
                                    "_summary._ssm_tested_donor_count",
                                    "_summary._total_donor_count")
                        )
                    )
            )
            .put(BaseDocumentType.GENE_SET_TEXT_TYPE,
                attributes()
                    .indexClassName(GeneSetTextIndexTask.class.getName())
                    .fields(fields()
                        .geneSetFields(
                            geneSetFields()
                                .includedFields(
                                    "id",
                                    "name",
                                    "type",
                                    "source",
                                    "go_term.alt_ids")
                        )
                    )
            )

            .put(BaseDocumentType.PROJECT_TYPE, attributes().indexClassName(ProjectIndexTask.class.getName()))
            .put(BaseDocumentType.PROJECT_TEXT_TYPE,
                attributes()
                    .indexClassName(ProjectTextIndexTask.class.getName())
                    .fields(fields()
                        .projectFields(
                            projectFields()
                                .includedFields(
                                    "_project_id",
                                    "project_name",
                                    "tumour_type",
                                    "tumour_subtype",
                                    "primary_site",
                                    "_summary._state")
                        )
                    )
            )

            .put(BaseDocumentType.DONOR_TYPE,
                attributes()
                    .indexClassName(DonorIndexTask.class.getName())
                    .broadcastDependencies(ImmutableList.of(BroadcastType.PROJECT))
                    .fields(fields()
                        .donorFields(
                            donorFields()
                                .excludedFields(
                                    "_id",
                                    "gene")
                        )
                    )
            )
            .put(BaseDocumentType.DONOR_TEXT_TYPE,
                attributes()
                    .indexClassName(DonorTextIndexTask.class.getName())
                    .fields(fields()
                        .donorFields(
                            donorFields()
                                .includedFields(
                                    "_donor_id",
                                    "_project_id",
                                    "donor_id",
                                    "specimen._specimen_id",
                                    "specimen.specimen_id",
                                    "specimen.sample._sample_id",
                                    "specimen.sample.analyzed_sample_id",
                                    "_summary._state")
                        )
                    )
            )
            .put(BaseDocumentType.DONOR_CENTRIC_TYPE,
                attributes()
                    .indexClassName(DonorCentricIndexTask.class.getName())
                    .broadcastDependencies(ImmutableList.of(BroadcastType.GENE, BroadcastType.PROJECT))
                    .fields(fields()
                        .projectFields(
                            projectFields()
                                .includedFields(
                                    // Primary key
                                    "_project_id",

                                    // Data
                                    "primary_site",
                                    "project_name")
                        )
                        .donorFields(
                            donorFields()
                                .includedFields(
                                    // Primary key
                                    "_donor_id",

                                    // Foreign keys
                                    "_project_id",

                                    // Summary
                                    "gene._gene_id",
                                    "gene._summary._ssm_count",
                                    "_summary",

                                    // Data
                                    "donor_id",
                                    "disease_status_last_followup",
                                    "donor_age_at_diagnosis",
                                    "donor_age_at_enrollment",
                                    "donor_age_at_last_followup",
                                    "donor_diagnosis_icd10",
                                    "donor_interval_of_last_followup",
                                    "donor_relapse_interval",
                                    "donor_relapse_type",
                                    "donor_sex",
                                    "donor_survival_time",
                                    "donor_tumour_stage_at_diagnosis",
                                    "donor_tumour_stage_at_diagnosis_supplemental",
                                    "donor_tumour_staging_system_at_diagnosis",
                                    "donor_vital_status")
                        )
                        .geneFields(
                            geneFields()
                                .includedFields(
                                    // Primary key
                                    "_gene_id",

                                    // Data
                                    "symbol",
                                    "biotype",
                                    "chromosome",
                                    "start",
                                    "end",

                                    // Gene sets
                                    "sets.id",
                                    "sets.type")
                        )
                        .observationFields(
                            observationFields()
                                .includedFields(
                                    // Foreign keys
                                    "_mutation_id",
                                    "_donor_id", // TODO: New from ETL1! Needed due to lack of MongoDB
                                    "consequence._gene_id",

                                    // Data
                                    "consequence.consequence_type",
                                    "consequence.functional_impact_prediction_summary",
                                    "_type",
                                    "mutation_type",
                                    "platform",
                                    "validation_status",
                                    "verification_status",
                                    "chromosome",
                                    "chromosome_end",
                                    "chromosome_start",
                                    "observation")
                        )
                    )
            )

            .put(BaseDocumentType.GENE_TYPE,
                attributes()
                    .indexClassName(GeneIndexTask.class.getName())
                    .fields(fields()
                        .geneFields(geneFields()
                            .excludedFields("donor")
                        )
                        .observationFields(
                            observationFields()
                                .excludedFields(
                                    "_id",
                                    "_summary",
                                    "project",
                                    "donor")
                        )
                    )
            )
            .put(BaseDocumentType.GENE_TEXT_TYPE,
                attributes()
                    .indexClassName(GeneTextIndexTask.class.getName())
                    .fields(fields()
                        .geneFields(
                            geneFields()
                                .includedFields(
                                    "_gene_id",
                                    "symbol",
                                    "name",
                                    "synonyms",
                                    "external_db_ids")
                        )
                    )
            )
            .put(BaseDocumentType.GENE_CENTRIC_TYPE,
                attributes()
                    .indexClassName(GeneCentricIndexTask.class.getName())
                    .broadcastDependencies(ImmutableList.of(BroadcastType.DONOR, BroadcastType.PROJECT))
                    .fields(fields()
                        .donorFields(
                            donorFields()
                                .excludedFields(
                                    "_id",
                                    "gene",
                                    "specimen")
                        )
                        .observationFields(
                            observationFields()
                                .excludedFields(
                                    "_id",
                                    "functional_impact_prediction_summary",
                                    "consequence.functional_impact_prediction")
                        )
                    )
            )

            .put(
                BaseDocumentType.OBSERVATION_CENTRIC_TYPE,
                attributes()
                    .indexClassName(ObservationCentricIndexTask.class.getName())
                    .broadcastDependencies(of(BroadcastType.DONOR, BroadcastType.PROJECT, BroadcastType.GENE))
                    .fields(fields()
                        .projectFields(
                            projectFields()
                                .includedFields(
                                    "_project_id",
                                    "project_name",
                                    "primary_site")
                        )
                        .donorFields(
                            donorFields()
                                .excludedFields(
                                    "_id",
                                    "gene",
                                    "specimen")
                        )
                        .geneFields(
                            geneFields()
                                .excludedFields(
                                    "_id",
                                    "project",
                                    "donor",
                                    "transcripts")

                        )
                        .observationFields(
                            observationFields()
                                .excludedFields(
                                    "_id",
                                    "functional_impact_prediction_summary",
                                    "consequence.functional_impact_prediction")
                        )
                    )
            )

            .put(BaseDocumentType.MUTATION_TEXT_TYPE,
                attributes()
                    .indexClassName(MutationTextIndexTask.class.getName())
                    .broadcastDependencies(ImmutableList.of(BroadcastType.GENE))
                    .fields(fields()
                        .mutationFields(
                            mutationFields()
                                .includedFields(
                                    "_mutation_id",
                                    "mutation",
                                    "chromosome",
                                    "chromosome_start")
                        )

                        .observationFields(
                            observationFields()
                                .includedFields(
                                    "_mutation_id",
                                    "consequence._gene_id",
                                    "consequence.aa_mutation",
                                    "mutation")
                        )
                        .geneFields(
                            geneFields()
                                .includedFields(
                                    "_gene_id",
                                    "symbol")
                        )
                    )
            )
            .put(
                BaseDocumentType.MUTATION_CENTRIC_TYPE,
                attributes()
                    .indexClassName(MutationCentricIndexTask.class.getName())
                    .broadcastDependencies(of(BroadcastType.DONOR, BroadcastType.PROJECT, BroadcastType.GENE))
                    .fields(fields()
                        .donorFields(
                            donorFields()
                                .excludedFields(
                                    "_id",
                                    "gene",
                                    "specimen")
                        )
                        .geneFields(
                            geneFields()
                                .excludedFields(
                                    "_id",
                                    "project",
                                    "donor",
                                    "transcripts.domains",
                                    "transcripts.exons")

                        )
                    )
            )
            .build();

    checkState(documentTypes.size() == BaseDocumentType.values().length);

    return documentTypes;
  }

  private static DocumentTypeAttributes attributes() {
    return new DocumentTypeAttributes();
  }

  private static DocumentFields.Builder fields() {
    return DocumentFields.documentFields();
  }

  private static CollectionFields.Builder geneFields() {
    return CollectionFields.collectionFields();
  }

  private static CollectionFields.Builder projectFields() {
    return CollectionFields.collectionFields();
  }

  private static CollectionFields.Builder donorFields() {
    return CollectionFields.collectionFields();
  }

  private static CollectionFields.Builder observationFields() {
    return CollectionFields.collectionFields();
  }

  private static CollectionFields.Builder mutationFields() {
    return CollectionFields.collectionFields();
  }

  private static CollectionFields.Builder geneSetFields() {
    return CollectionFields.collectionFields();
  }

}
