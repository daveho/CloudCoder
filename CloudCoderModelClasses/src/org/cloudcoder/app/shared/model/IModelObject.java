package org.cloudcoder.app.shared.model;

/**
 * Interface implemented by all model object classes capable of being
 * persisted and serialized as XML.
 * 
 * @author David Hovemeyer
 *
 * @param <E> the model object class's actual type
 */
public interface IModelObject<E extends IModelObject<E>> {
	/**
	 * Get this object's {@link ModelObjectSchema} describing its fields.
	 * 
	 * @return the object's {@link ModelObjectSchema}
	 */
	public ModelObjectSchema<? super E> getSchema();
}
