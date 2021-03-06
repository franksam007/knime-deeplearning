/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Jun 6, 2017 (marcel): created
 */
package org.knime.dl.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.knime.dl.util.DLUtils.Preconditions.checkNotNullOrEmpty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.knime.core.util.FileUtil;
import org.knime.core.util.Version;
import org.knime.dl.core.DLDefaultFixedTensorShape;
import org.knime.dl.core.DLDefaultPartialTensorShape;
import org.knime.dl.core.DLException;
import org.knime.dl.core.DLFixedTensorShape;
import org.knime.dl.core.DLNetworkSpec;
import org.knime.dl.core.DLPartialTensorShape;
import org.knime.dl.core.DLTensorId;
import org.knime.dl.core.DLTensorShape;
import org.knime.dl.core.DLTensorSpec;
import org.knime.dl.core.DLUnknownTensorShape;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Various utility methods and classes.
 *
 * @author Marcel Wiedenmann, KNIME GmbH, Konstanz, Germany
 * @author Christian Dietz, KNIME GmbH, Konstanz, Germany
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class DLUtils {

    private DLUtils() {
    }

    public static class Files {

        private Files() {
        }

        public static File getFileFromBundle(final String bundleName, final String relativePath) throws IOException {
            checkNotNullOrEmpty(bundleName);
            checkNotNullOrEmpty(relativePath);
            try {
                final Bundle bundle = Platform.getBundle(bundleName);
                final URL url = FileLocator.find(bundle, new Path(relativePath), null);
                return url != null ? FileUtil.getFileFromURL(FileLocator.toFileURL(url)) : null;
            } catch (final Exception e) {
                throw new IOException(
                    "Failed to get file '" + relativePath + "' from bundle '" + bundleName + "': " + e.getMessage(), e);
            }
        }

        public static File getFileFromSameBundle(final Class<?> classFromBundle, final String relativePath)
            throws IllegalArgumentException, IOException {
            checkNotNull(classFromBundle);
            checkNotNullOrEmpty(relativePath);
            final Bundle bundle = FrameworkUtil.getBundle(classFromBundle);
            if (bundle == null) {
                throw new IllegalArgumentException(
                    "Failed to get file '" + relativePath + "' from the bundle of class '"
                        + classFromBundle.getCanonicalName() + "'. Bundle could not be resolved.");
            }
            return DLUtils.Files.getFileFromBundle(bundle.getSymbolicName(), relativePath);
        }

        public static File getFileFromSameBundle(final Object caller, final String relativePath)
            throws IllegalArgumentException, IOException {
            return getFileFromSameBundle(caller.getClass(), relativePath);
        }

        public static String readAllUTF8(final File f) throws IOException {
            checkNotNull(f);
            return new String(java.nio.file.Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        }
    }

    public static class Layers {

        private Layers() {
        }
    }

    public static class Networks {

        private Networks() {
        }

        public static Optional<DLTensorSpec> findTensorSpecByName(final String tensorName,
            final DLNetworkSpec networkSpec) {
            return findTensorSpecByName(tensorName, networkSpec.getInputSpecs(), networkSpec.getHiddenOutputSpecs(),
                networkSpec.getOutputSpecs());
        }

        public static Optional<DLTensorSpec> findTensorSpecByName(final String tensorName,
            final DLTensorSpec[]... tensorSpecs) {
            checkNotNullOrEmpty(tensorName);
            checkNotNull(tensorSpecs);
            return Arrays.stream(tensorSpecs).flatMap(Arrays::stream).filter(s -> s.getName().equals(tensorName))
                .findFirst();
        }

        public static Optional<DLTensorSpec> findTensorSpecById(final DLTensorId tensorId,
            final DLNetworkSpec networkSpec) {
            return findTensorSpecById(tensorId, networkSpec.getInputSpecs(), networkSpec.getHiddenOutputSpecs(),
                networkSpec.getOutputSpecs());
        }

        public static Optional<DLTensorSpec> findTensorSpecById(final DLTensorId tensorId,
            final DLTensorSpec[]... tensorSpecs) {
            checkNotNull(tensorId);
            checkNotNull(tensorSpecs);
            return Arrays.stream(tensorSpecs).flatMap(Arrays::stream).filter(s -> s.getIdentifier().equals(tensorId))
                .findFirst();
        }

        public static OptionalInt findTensorSpecIndex(final DLTensorSpec spec, final DLTensorSpec... specs) {
            checkNotNull(spec);
            checkNotNull(specs);

            for (int i = 0; i < specs.length; i++) {
                if (spec.equals(specs[i])) {
                    return OptionalInt.of(i);
                }
            }
            return OptionalInt.empty();
        }

        public static OptionalInt findTensorSpecIndexById(final DLTensorSpec spec, final DLTensorSpec... specs) {
            checkNotNull(spec);
            checkNotNull(specs);
            final DLTensorId id = spec.getIdentifier();
            for (int i = 0; i < specs.length; i++) {
                if (id.equals(specs[i].getIdentifier())) {
                    return OptionalInt.of(i);
                }
            }
            return OptionalInt.empty();
        }
    }

    /**
     * Utility class that helps checking whether the preconditions of a method or constructor invocation have been met
     * by the caller. <br>
     * This class complements the functionality of {@link com.google.common.base.Preconditions}.
     */
    public static class Preconditions {

        private Preconditions() {
        }

        /**
         * Ensures that a string passed as a parameter to the calling method is not null or empty.
         *
         * @param string a string
         * @return the non-null and non-empty reference that was validated
         * @throws NullPointerException if the input is null
         * @throws IllegalArgumentException if the input is empty
         * @see com.google.common.base.Preconditions#checkNotNull(Object)
         */
        public static String checkNotNullOrEmpty(final String string) {
            if (string == null) {
                throw new NullPointerException();
            }
            if (string.isEmpty()) {
                throw new IllegalArgumentException();
            }
            return string;
        }

        /**
         * Ensures that a string passed as a parameter to the calling method is not null or empty.
         *
         * @param string a string
         * @param errorMessage the exception message to use if the check fails; will be converted to a string using
         *            {@link String#valueOf(Object)}
         * @return the non-null and non-empty reference that was validated
         * @throws NullPointerException if the input is null
         * @throws IllegalArgumentException if the input is empty
         * @see com.google.common.base.Preconditions#checkNotNull(Object, Object))
         */
        public static String checkNotNullOrEmpty(final String string, final Object errorMessage) {
            if (string == null) {
                throw new NullPointerException(String.valueOf(errorMessage));
            }
            if (string.isEmpty()) {
                throw new IllegalArgumentException(String.valueOf(errorMessage));
            }
            return string;
        }

        /**
         * Ensures that a collection passed as a parameter to the calling method is not null or empty.
         *
         * @param collection a collection
         * @return the non-null and non-empty reference that was validated
         * @throws NullPointerException if the input is null
         * @throws IllegalArgumentException if the input is empty
         * @see com.google.common.base.Preconditions#checkNotNull(Object)
         */
        public static <C extends Collection<?>> C checkNotNullOrEmpty(final C collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            if (collection.isEmpty()) {
                throw new IllegalArgumentException();
            }
            return collection;
        }

        /**
         * Ensures that a collection passed as a parameter to the calling method is not null or empty.
         *
         * @param collection a collection
         * @param errorMessage the exception message to use if the check fails; will be converted to a string using
         *            {@link String#valueOf(Object)}
         * @return the non-null and non-empty reference that was validated
         * @throws NullPointerException if the input is null
         * @throws IllegalArgumentException if the input is empty
         * @see com.google.common.base.Preconditions#checkNotNull(Object, Object))
         */
        public static <C extends Collection<?>> C checkNotNullOrEmpty(final C collection, final Object errorMessage) {
            if (collection == null) {
                throw new NullPointerException(String.valueOf(errorMessage));
            }
            if (collection.isEmpty()) {
                throw new IllegalArgumentException(String.valueOf(errorMessage));
            }
            return collection;
        }
    }

    public static class Shapes {

        public static final String UNKNOWN_DIM_SIZE_REPR = "?";

        private Shapes() {
        }

        /**
         *
         *
         * @param shape the shape of the tensor as Long array. Does not include the batch size. May be <code>null</code>
         *            or empty in which case it is interpreted as {@link DLUnknownTensorShape unknown shape}. May
         *            contain <code>null</code> elements which are interpreted as {@link DLPartialTensorShape unknown
         *            dimensions}.
         * @return the shape
         */
        public static DLTensorShape shapeFromLongArray(final Long... shape) {
            if (shape == null || shape.length == 0) {
                return DLUnknownTensorShape.INSTANCE;
            }
            boolean hasUnknownDim = false;
            for (int i = 0; i < shape.length; i++) {
                if (shape[i] == null) {
                    hasUnknownDim = true;
                    break;
                }
            }
            if (hasUnknownDim) {
                return new DLDefaultPartialTensorShape(Arrays.stream(shape)
                    .map(l -> l != null ? OptionalLong.of(l) : OptionalLong.empty()).toArray(l -> new OptionalLong[l]));
            } else {
                return new DLDefaultFixedTensorShape(Arrays.stream(shape).mapToLong(l -> l).toArray());
            }
        }

        /**
         * @param shape the shape of the tensor. Must be either one of {@link DLFixedTensorShape},
         *            {@link DLPartialTensorShape}, or {@link DLUnknownTensorShape}.
         * @return the shape of the tensor as Long array, may be empty (unknown shape) or contain <code>null</code>
         *         elements (partially defined shape)
         */
        public static Long[] shapeToLongArray(final DLTensorShape shape) {
            if (shape == null || shape == DLUnknownTensorShape.INSTANCE) {
                return new Long[0];
            }
            if (shape instanceof DLFixedTensorShape) {
                return Arrays.stream(((DLFixedTensorShape)shape).getShape()).mapToObj(l -> (Long)l)
                    .toArray(l -> new Long[l]);
            }
            if (isPartial(shape)) {
                return Arrays.stream(((DLPartialTensorShape)shape).getShape())
                    .map(opt -> opt.isPresent() ? opt.getAsLong() : null).toArray(Long[]::new);
            }
            throw new UnsupportedOperationException("Tensor shape type '" + shape.getClass().getSimpleName()
                + "' is not supported yet. This is an implementation error.");
        }

        public static boolean isKnown(final DLTensorShape shape) {
            return !(shape instanceof DLUnknownTensorShape);
        }

        public static boolean isFixed(final DLTensorShape shape) {
            return shape instanceof DLFixedTensorShape;
        }

        public static boolean isPartial(final DLTensorShape shape) {
            return shape instanceof DLPartialTensorShape;
        }

        public static Optional<long[]> getFixedShape(final DLTensorShape shape) {
            if (isFixed(shape)) {
                return Optional.of(((DLFixedTensorShape)shape).getShape());
            }
            return Optional.empty();
        }

        public static OptionalLong getFixedSize(final DLTensorShape shape) {
            final Optional<long[]> fixedShape = getFixedShape(shape);
            if (fixedShape.isPresent()) {
                return OptionalLong.of(getSize(fixedShape.get()));
            }
            return OptionalLong.empty();
        }

        public static OptionalLong getDimSize(final DLTensorShape shape, final int dim) {
            checkArgument(dim > -1, "The dimension index must be greater or equal zero.");
            checkArgument(shape.getNumDimensions() > dim, "The dimension index %s exceeds the size of shape %s", dim,
                shape);
            if (isFixed(shape)) {
                return OptionalLong.of(((DLFixedTensorShape)shape).getShape()[dim]);
            } else if (isPartial(shape)) {
                return ((DLPartialTensorShape)shape).getDimension(dim);
            } else {
                throw new IllegalArgumentException("Unsupported shape " + shape);
            }
        }

        public static long getSize(final long[] shape) {
            if (shape == null || shape.length == 0) {
                return 0;
            }
            long size = 1;
            for (int i = 0; i < shape.length; i++) {
                size *= shape[i];
            }
            return size;
        }

        public static String getSizeAsString(final DLTensorShape shape) {
            final OptionalLong nn = DLUtils.Shapes.getFixedSize(shape);
            return nn.isPresent() ? Long.toString(nn.getAsLong()) : DLUtils.Shapes.UNKNOWN_DIM_SIZE_REPR;
        }

        public static long[] calculateExecutionShape(final DLTensorShape tensorShape, final long[] dataShape) {
            if (isFixed(tensorShape)) {
                final long[] ts = ((DLFixedTensorShape)tensorShape).getShape();
                checkArgument(getSize(ts) == getSize(dataShape),
                    "The input shape does not match the tensor shape. %s vs. %s", Arrays.toString(dataShape),
                    tensorShape);
                return ts;
            } else if (isPartial(tensorShape)) {
                final Optional<long[]> executionShape =
                    executionShapeFromPartialShape((DLPartialTensorShape)tensorShape, dataShape);
                checkArgument(executionShape.isPresent(), "The input shape does not match the tensor shape. %s vs. %s",
                    Arrays.toString(dataShape), tensorShape);
                return executionShape.get();
            }
            throw new IllegalArgumentException("Currently only known shapes are supported.");
        }

        public static Optional<long[]> executionShapeFromPartialShape(final DLPartialTensorShape tensorShape,
            final long[] dataShape) {
            if (dataShape.length == 1) {
                if (tensorShape.getNumUnknownDimensions() == 1) {
                    final long nInputs = dataShape[0];
                    if (nInputs % tensorShape.getKnownSize() == 0L) {
                        final long[] executionShape =
                            IntStream.range(0, tensorShape.getNumDimensions()).mapToObj(tensorShape::getDimension)
                                .mapToLong(o -> o.isPresent() ? o.getAsLong() : nInputs / tensorShape.getKnownSize())
                                .toArray();
                        return Optional.of(executionShape);
                    }
                }
            } else if (dataShape.length > 1) {
                final long dataSize = getSize(dataShape);
                // TODO: Figure out how to best match dimensions
                if (dataShape.length == tensorShape.getNumDimensions() && dataSize % tensorShape.getKnownSize() == 0) {
                    return Optional.of(dataShape);
                }
            }
            return Optional.empty();
        }
    }

    public static class Strings {

        private Strings() {
        }

        /**
         * Joins the elements of the provided collection into a single string. No delimiter is added before or after the
         * elements.
         *
         * @param collection the collection providing the elements to join together
         * @param separator the separator to use, <code>null</code> is treated as empty string
         * @param maxNumElems the maximum number of elements to join together. Excessive elements will be omitted and an
         *            ellipsis ("...") will be added to the resulting string.
         * @return the joined string
         */
        public static <T> String joinAbbreviated(final Collection<T> collection, final String separator,
            final int maxNumElems) {
            if (collection.size() <= maxNumElems) {
                return StringUtils.join(collection, separator);
            } else {
                final Iterator<T> iterator = new Iterator<T>() {

                    Iterator<T> m_delegate = collection.iterator();

                    int m_i = 0;

                    @Override
                    public boolean hasNext() {
                        return m_delegate.hasNext() && m_i < maxNumElems;
                    }

                    @Override
                    public T next() {
                        final T elem = m_delegate.next();
                        m_i++;
                        return elem;
                    }
                };
                return StringUtils.join(iterator, separator) + separator + "...";
            }
        }
    }

    public static class Misc {

        private Misc() {
        }

        /**
         * Finds the latest {@link DLException} in a {@link Throwable#getCause() chain} of throwables and returns its
         * message.
         *
         * @param throwable the last throwable in the chain of throwables
         * @return an optional that contains the error message if one is found
         */
        public static Optional<String> findDisplayableErrorMessage(final Throwable throwable) {
            if (throwable == null) {
                return Optional.empty();
            }
            Throwable t = throwable;
            do {
                if (t instanceof DLException) {
                    return Optional.of(((DLException)t).getMessage());
                }
            } while (t != t.getCause() && (t = t.getCause()) != null);
            return Optional.empty();
        }

        /**
         * @param classFromBundle the class of whose bundle to retrieve the version
         * @return the version of the bundle of the argument. Returns an all-zero version if the bundle version is
         *         unspecified.
         * @throws IllegalArgumentException if the bundle of the argument could not be resolved
         */
        public static Version getVersionOfSameBundle(final Class<?> classFromBundle) {
            checkNotNull(classFromBundle);
            final Bundle bundle = FrameworkUtil.getBundle(classFromBundle);
            if (bundle == null) {
                throw new IllegalArgumentException("Failed to get version from the bundle of class '"
                    + classFromBundle.getCanonicalName() + "'. Bundle could not be resolved.");
            }
            final org.osgi.framework.Version osgiVersion = bundle.getVersion();
            if (osgiVersion == org.osgi.framework.Version.emptyVersion) {
                return new Version(0, 0, 0);
            } else {
                return new Version(osgiVersion.getMajor(), osgiVersion.getMinor(), osgiVersion.getMicro());
            }
        }
    }
}
