/*
 * Copyright (c) 2014 The Ontario Institute for Cancer Research. All rights reserved.
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
package org.icgc.dcc.release.job.document.transform;

import static org.icgc.dcc.release.core.util.ObjectNodes.MAPPER;

import org.apache.spark.api.java.function.Function;
import org.icgc.dcc.release.core.document.Document;
import org.icgc.dcc.release.core.document.DocumentType;
import org.icgc.dcc.release.job.document.context.DefaultDocumentContext;
import org.icgc.dcc.release.job.document.core.DocumentContext;
import org.icgc.dcc.release.job.document.core.DocumentJobContext;
import org.icgc.dcc.release.job.document.core.DocumentTransform;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.NonNull;
import lombok.val;

public class GeneSetTextDocumentTransform implements DocumentTransform, Function<ObjectNode, Document> {

  private final DocumentContext documentContext;

  public GeneSetTextDocumentTransform(@NonNull DocumentJobContext documentJobContext) {
    this.documentContext = new DefaultDocumentContext(DocumentType.GENE_SET_TEXT_TYPE, documentJobContext);
  }

  @Override
  public Document call(ObjectNode geneSet) throws Exception {
    return transformDocument(geneSet, documentContext);
  }

  @Override
  public Document transformDocument(@NonNull ObjectNode geneSet, @NonNull DocumentContext context) {
    val type = context.getType();
    val id = geneSet.get("id").textValue();

    geneSet.set("id", geneSet.remove("id"));
    geneSet.set("name", geneSet.remove("name"));
    geneSet.set("source", geneSet.remove("source"));
    geneSet.set("type", geneSet.remove("type"));

    if (!geneSet.path("go_term").isMissingNode()) {
      geneSet.set("altIds", geneSet.get("go_term").get("alt_ids"));
      geneSet.remove("go_term");
    }

    val geneSetText = MAPPER.createObjectNode();
    geneSetText.set("text", geneSet);

    return new Document(type, id, geneSetText);
  }

}